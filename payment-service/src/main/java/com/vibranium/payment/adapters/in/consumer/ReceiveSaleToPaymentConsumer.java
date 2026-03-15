package com.vibranium.payment.adapters.in.consumer;

import com.vibranium.payment.adapters.out.message.SaleMessage;
import com.vibranium.payment.application.core.domain.Sale;
import com.vibranium.payment.application.core.domain.enums.SaleEvent;
import com.vibranium.payment.application.ports.in.SalePaymentInputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReceiveSaleToPaymentConsumer {

    private final SalePaymentInputPort salePaymentInputPort;

    public ReceiveSaleToPaymentConsumer(SalePaymentInputPort salePaymentInputPort) {
        this.salePaymentInputPort = salePaymentInputPort;
    }

    @KafkaListener(topics = "tp-saga-payment", groupId = "payment")
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "receiveFallback")
    public void receive(SaleMessage saleMessage) {
        if (!SaleEvent.EXECUTE_PAYMENT.equals(saleMessage.getEvent())) {
            return;
        }

        Sale sale = saleMessage.getSale();

        log.info(
                "Starting payment processing. saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                sale.getId(),
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );

        // Se der erro aqui, a exceção sobe para o Circuit Breaker
        salePaymentInputPort.payment(sale);

        log.info(
                "Payment processed successfully. saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                sale.getId(),
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );
    }

    /**
     * Fallback called when:
     * - the "kafkaAccess" circuit is OPEN, or
     * - an exception thrown in receive(...) is counted by the Circuit Breaker.
     */
    private void receiveFallback(SaleMessage saleMessage, Throwable t) {
        Sale sale = saleMessage.getSale();

        log.error(
                "Error processing payment event from Kafka. " +
                        "saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                sale != null ? sale.getId() : null,
                sale != null ? sale.getUserId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                sale != null ? sale.getValue() : null,
                t
        );

    }
}