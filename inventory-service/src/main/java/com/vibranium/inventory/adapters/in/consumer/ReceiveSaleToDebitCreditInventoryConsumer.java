package com.vibranium.inventory.adapters.in.consumer;

import com.vibranium.inventory.adapters.out.message.SaleMessage;
import com.vibranium.inventory.application.core.domain.Sale;
import com.vibranium.inventory.application.core.domain.enums.SaleEvent;
import com.vibranium.inventory.application.core.domain.enums.TypeSale;
import com.vibranium.inventory.application.ports.in.CreditInventoryInputPort;
import com.vibranium.inventory.application.ports.in.DebitInventoryInputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiveSaleToDebitCreditInventoryConsumer {

    private final DebitInventoryInputPort debitInventoryInputPort;
    private final List<CreditInventoryInputPort> creditInventoryWorkflows;

    @KafkaListener(topics = "tp-saga-inventory", groupId = "inventory-debit")
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "receiveFallback")
    public void receive(SaleMessage saleMessage) {

        if (saleMessage == null || saleMessage.getSale() == null) {
            log.warn("Invalid Kafka message received: sale or saleMessage is null.");
            return;
        }

        Sale sale = saleMessage.getSale();
        SaleEvent event = saleMessage.getEvent();
        TypeSale type = sale.getType();

        log.info(
                "Starting inventory processing | saleId={} | event={} | type={}",
                sale.getId(),
                event,
                type
        );

        if (shouldDebitInventory(event, type)) {
            processDebit(sale);
        } else {
            processCredit(sale, event, type);
        }

        log.info(
                "Inventory processing finished successfully | saleId={}",
                sale.getId()
        );
    }

    /**
     * Fallback called when:
     * - the "kafkaAccess" circuit is OPEN, or
     * - an exception thrown in receive(...) is counted by the Circuit Breaker.
     */
    private void receiveFallback(SaleMessage saleMessage, Throwable t) {
        Sale sale = saleMessage != null ? saleMessage.getSale() : null;
        SaleEvent event = saleMessage != null ? saleMessage.getEvent() : null;
        TypeSale type = (sale != null) ? sale.getType() : null;

        log.error(
                "Error processing inventory (debit/credit) event from Kafka. " +
                        "saleId={} | productId={} | sellerId={} | offerId={} | event={} | type={} | quantity={}",
                sale != null ? sale.getId() : null,
                sale != null ? sale.getProductId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                event,
                type,
                sale != null ? sale.getQuantity() : null,
                t
        );

        throw new RuntimeException(
                "Error processing inventory (debit/credit) event from Kafka. " +
                        "Kafka consumer is unavailable or circuit breaker is open.",
                t
        );
    }

    private boolean shouldDebitInventory(SaleEvent event, TypeSale type) {
        return SaleEvent.PREPARE_INVENTORY.equals(event)
                && TypeSale.BUY.equals(type);
    }

    private void processDebit(Sale sale) {
        log.info(
                "Inventory debit started | saleId={} | quantity={}",
                sale.getId(),
                sale.getQuantity()
        );

        debitInventoryInputPort.debit(sale);

        log.info(
                "Inventory debit finished | saleId={}",
                sale.getId()
        );
    }

    private void processCredit(Sale sale, SaleEvent event, TypeSale type) {
        log.info(
                "Selecting credit workflow | saleId={} | event={} | type={}",
                sale.getId(),
                event,
                type
        );

        CreditInventoryInputPort workflow = creditInventoryWorkflows.stream()
                .filter(w -> w.getType(type))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No credit workflow found for TypeSale: " + type
                        )
                );

        workflow.credit(sale);

        log.info(
                "Inventory credit finished | saleId={} | quantity={}",
                sale.getId(),
                sale.getQuantity()
        );
    }
}