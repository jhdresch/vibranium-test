package com.vibranium.orchestrator.adapters.out;

import com.vibranium.orchestrator.adapters.out.message.SaleMessage;
import com.vibranium.orchestrator.application.core.domain.Sale;
import com.vibranium.orchestrator.application.core.domain.enums.SaleEvent;
import com.vibranium.orchestrator.application.ports.out.SendSaleToTopicOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendSaleToTopicAdapter implements SendSaleToTopicOutputPort {

    private final KafkaTemplate<String, SaleMessage> kafkaTemplate;

    public SendSaleToTopicAdapter(KafkaTemplate<String, SaleMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "sendFallback")
    public void send(Sale sale, SaleEvent saleEvent, String topic) {

        log.info(
                "Sending SaleMessage to Kafka. topic={}, event={}, saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                topic,
                saleEvent,
                sale.getId(),
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );

        var saleMessage = new SaleMessage(sale, saleEvent);
        kafkaTemplate.send(topic, saleMessage);

        log.info(
                "SaleMessage sent to Kafka successfully. topic={}, event={}, saleId={}",
                topic,
                saleEvent,
                sale.getId()
        );
    }

    /**
     * Fallback called when:
     * - the "kafkaAccess" circuit is OPEN, or
     * - an exception thrown in send(...) is counted by the Circuit Breaker.
     */
    private void sendFallback(Sale sale, SaleEvent saleEvent, String topic, Throwable t) {

        log.error(
                "Error while sending SaleMessage to Kafka. " +
                        "topic={}, event={}, saleId={}, userId={}, sellerId={}, offerId={}, value={}",
                topic,
                saleEvent,
                sale != null ? sale.getId() : null,
                sale != null ? sale.getUserId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getOfferId() : null,
                sale != null ? sale.getValue() : null,
                t
        );
    }
}