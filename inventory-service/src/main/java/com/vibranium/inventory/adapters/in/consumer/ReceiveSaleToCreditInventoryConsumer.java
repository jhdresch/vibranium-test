package com.vibranium.inventory.adapters.in.consumer;

import com.vibranium.inventory.adapters.out.message.SaleMessage;
import com.vibranium.inventory.application.core.domain.enums.SaleEvent;
import com.vibranium.inventory.application.ports.in.CreditInventoryInputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ReceiveSaleToCreditInventoryConsumer {

    private final List<CreditInventoryInputPort> creditInventoryWorkflows;

    public ReceiveSaleToCreditInventoryConsumer(List<CreditInventoryInputPort> creditInventoryWorkflows) {
        this.creditInventoryWorkflows = creditInventoryWorkflows;
    }

    @KafkaListener(topics = "tp-saga-inventory", groupId = "inventory-credit")
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "receiveFallback")
    public void receive(SaleMessage saleMessage) {

        if (!SaleEvent.EXECUTE_ROLLBACK.equals(saleMessage.getEvent())) {
            return;
        }

        var sale = saleMessage.getSale();

        log.info(
                "Starting inventory credit (rollback). saleId={}, productId={}, sellerId={}, offerId={}, type={}, quantity={}",
                sale.getId(),
                sale.getProductId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getType(),
                sale.getQuantity()
        );

        var workflow = creditInventoryWorkflows.stream()
                .filter(c -> c.getType(saleMessage.getSale().getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No credit workflow found for TypeSale: " + saleMessage.getSale().getType()
                ));

        log.info(
                "Credit workflow found for TypeSale. saleId={}, type={}",
                sale.getId(),
                sale.getType()
        );

        // Se der erro aqui (workflow.credit), a exceção sobe para o Circuit Breaker
        workflow.credit(saleMessage.getSale());

        log.info(
                "Inventory credit (rollback) finished successfully. saleId={}, quantity={}",
                sale.getId(),
                sale.getQuantity()
        );
    }

    /**
     * Fallback called when:
     * - the "kafkaAccess" circuit is OPEN, or
     * - an exception thrown in receive(...) is counted by the Circuit Breaker.
     */
    private void receiveFallback(SaleMessage saleMessage, Throwable t) {
        var sale = saleMessage.getSale();

        log.error(
                "Error processing inventory credit (rollback) event from Kafka. " +
                        "saleId={}, productId={}, sellerId={}, offerId={}, type={}, quantity={}",
                sale != null ? sale.getId() : null,
                sale != null ? sale.getProductId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                sale != null ? sale.getType() : null,
                sale != null ? sale.getQuantity() : null,
                t
        );

    }
}