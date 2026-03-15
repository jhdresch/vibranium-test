package com.vibranium.sale.adapters.in.consumer;

import com.vibranium.sale.adapters.out.message.SaleMessage;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.core.domain.enums.SaleEvent;
import com.vibranium.sale.application.ports.in.FinalizeSaleInputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiveSaleToFinalizeConsumer {

    private final FinalizeSaleInputPort finalizeSaleInputPort;

    @KafkaListener(topics = "tp-saga-sale", groupId = "sale-finalize")
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "receiveFallback")
    public void receive(SaleMessage saleMessage) {

        if (!SaleEvent.FINALIZE_SALE.equals(saleMessage.getEvent())) {
            return;
        }

        Sale sale = saleMessage.getSale();

        log.info(
                "Starting sale finalization | saleId={} | userId={} | sellerId={} | offerId={} | value={}",
                sale.getId(),
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );

        // Se der erro aqui, a exceção sobe para o Circuit Breaker
        finalizeSaleInputPort.finalize(sale);

        log.info(
                "Sale finalized successfully | saleId={} | userId={} | sellerId={} | offerId={} | value={}",
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
                "Error finalizing sale from Kafka. " +
                        "saleId={} | userId={} | sellerId={} | offerId={} | value={}",
                sale != null ? sale.getId() : null,
                sale != null ? sale.getUserId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                sale != null ? sale.getValue() : null,
                t
        );

    }
}