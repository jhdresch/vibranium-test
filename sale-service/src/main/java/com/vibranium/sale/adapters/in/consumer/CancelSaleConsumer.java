package com.vibranium.sale.adapters.in.consumer;

import com.vibranium.sale.adapters.out.message.SaleMessage;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.core.domain.enums.SaleEvent;
import com.vibranium.sale.application.ports.in.CancelSaleInputPort;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CancelSaleConsumer {

    private final CancelSaleInputPort cancelSaleInputPort;

    public CancelSaleConsumer(CancelSaleInputPort cancelSaleInputPort) {
        this.cancelSaleInputPort = cancelSaleInputPort;
    }

    @KafkaListener(topics = "tp-saga-sale", groupId = "sale-cancel")
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "receiveFallback")
    public void receive(SaleMessage saleMessage) {
        if (saleMessage == null || saleMessage.getSale() == null) {
            log.warn("Invalid cancel sale message received from Kafka: saleMessage or sale is null.");
            return;
        }

        if (!SaleEvent.CANCEL_SALE.equals(saleMessage.getEvent())) {
            // Ignora outros eventos nesse tópico (se houver)
            return;
        }

        Sale sale = saleMessage.getSale();

        log.info(
                "Starting sale cancellation. saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                sale.getId(),
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );

        // Pode lançar Exception -> circuit breaker intercepta
        cancelSaleInputPort.cancel(sale);

        log.info(
                "Sale cancelled successfully. saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                sale.getId(),
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );
    }

    // Fallback quando o Circuit Breaker está OPEN
    @SuppressWarnings("unused")
    private void receiveFallback(SaleMessage saleMessage, CallNotPermittedException ex) {
        Sale sale = saleMessage != null ? saleMessage.getSale() : null;

        log.warn(
                "CircuitBreaker 'kafkaAccess' is OPEN. Skipping cancel sale event processing. " +
                        "saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                sale != null ? sale.getId() : null,
                sale != null ? sale.getUserId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                sale != null ? sale.getValue() : null,
                ex
        );
        // Não relança -> evita log de erro em loop por CB aberto
    }

    // Fallback genérico para erros reais no método receive
    @SuppressWarnings("unused")
    private void receiveFallback(SaleMessage saleMessage, Exception ex) {
        Sale sale = saleMessage != null ? saleMessage.getSale() : null;

        log.error(
                "Error processing cancel sale event from Kafka. " +
                        "saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                sale != null ? sale.getId() : null,
                sale != null ? sale.getUserId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                sale != null ? sale.getValue() : null,
                ex
        );

    }
}