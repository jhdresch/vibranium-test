package com.vibranium.inventory.adapters.out;

import com.vibranium.inventory.adapters.out.message.SaleMessage;
import com.vibranium.inventory.application.core.domain.Sale;
import com.vibranium.inventory.application.core.domain.enums.SaleEvent;
import com.vibranium.inventory.application.ports.out.SendToKafkaOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendToKafkaAdapter implements SendToKafkaOutputPort {

    private final KafkaTemplate<String, SaleMessage> kafkaTemplate;

    public SendToKafkaAdapter(KafkaTemplate<String, SaleMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "sendFallback")
    public void send(Sale sale, SaleEvent event) {
        try {
            var saleMessage = new SaleMessage(sale, event);

            log.info("Sending message to Kafka: saleId={}, event={}",
                    sale.getId(), event);

            kafkaTemplate.send("tp-saga-orchestrator", saleMessage)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send message asynchronously: saleId={}, event={}",
                                    sale.getId(), event, ex);
                        } else {
                            log.debug("Message sent successfully: saleId={}, event={}, partition={}, offset={}",
                                    sale.getId(),
                                    event,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception e) {
            log.error("Error sending message to Kafka synchronously: saleId={}, event={}",
                    sale.getId(), event, e);
        }
    }

    private void sendFallback(Sale sale, SaleEvent event, Throwable t) {
        log.error(
                "FALLBACK - Kafka unavailable or circuit breaker open. " +
                        "Sale saleId={}, userId={}, sellerId={}, productId={}, event={}",
                sale != null ? sale.getId() : null,
                sale != null ? sale.getUserId() : null,
                sale != null ? sale.getSellerId() : null,
                sale != null ? sale.getProductId() : null,
                event,
                t
        );
    }
}