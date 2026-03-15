package com.vibranium.orchestrator.adapters.in.consumer;

import com.vibranium.orchestrator.adapters.out.message.SaleMessage;
import com.vibranium.orchestrator.application.ports.in.WorkflowInputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ReceiveSaleToProcessConsumer {

    private final List<WorkflowInputPort> workflows;

    public ReceiveSaleToProcessConsumer(List<WorkflowInputPort> workflows) {
        this.workflows = workflows;
    }

    @KafkaListener(topics = "tp-saga-orchestrator", groupId = "orchestrator")
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "receiveFallback")
    public void receive(@Payload SaleMessage saleMessage) {

        if (saleMessage == null || saleMessage.getSale() == null) {
            log.warn("Invalid Kafka message received: sale or saleMessage is null.");
            return;
        }

        var event = saleMessage.getEvent();
        var sale = saleMessage.getSale();

        log.info(
                "Received event to process workflow. event={}, saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                event,
                sale.getId(),
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );

        var workflow = workflows.stream()
                .filter(w -> w.isCalledByTheEvent(event))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No workflow found for event: " + event
                ));

        log.info(
                "Executing workflow for event. event={}, saleId={}",
                event,
                sale.getId()
        );

        // Se der erro aqui, a exceção sobe para o Circuit Breaker
        workflow.execute(sale);

        log.info(
                "Workflow executed successfully. event={}, saleId={}",
                event,
                sale.getId()
        );
    }

    /**
     * Fallback called when:
     * - the "kafkaAccess" circuit is OPEN, or
     * - an exception thrown in receive(...) is counted by the Circuit Breaker.
     */
    private void receiveFallback(@Payload SaleMessage saleMessage, Throwable t) {
        var event = saleMessage != null ? saleMessage.getEvent() : null;
        var sale = saleMessage != null ? saleMessage.getSale() : null;

        log.error(
                "Error while executing workflow from Kafka. " +
                        "event={}, saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                event,
                sale != null ? sale.getId() : null,
                sale != null ? sale.getUserId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                sale != null ? sale.getValue() : null,
                t
        );

    }
}