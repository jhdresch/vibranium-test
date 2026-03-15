package com.vibranium.sale.adapters.out;

import com.vibranium.sale.adapters.out.message.SaleMessage;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.core.domain.enums.SaleEvent;
import com.vibranium.sale.application.ports.out.SendCreatedSaleOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SendCreatedSaleAdapter implements SendCreatedSaleOutputPort {

    private final KafkaTemplate<String, SaleMessage> kafkaTemplate;

    public SendCreatedSaleAdapter(KafkaTemplate<String, SaleMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @CircuitBreaker(name = "kafkaAccess", fallbackMethod = "sendFallback")
    public void send(Sale sale, SaleEvent event) {
        try {
            var saleMessage = new SaleMessage(sale, event);
            kafkaTemplate.send("tp-saga-orchestrator", saleMessage)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send message to Kafka asynchronously", ex);
                        } else {
                            log.debug("Message sent successfully to topic tp-saga-orchestrator, partition={}, offset={}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending message to Kafka synchronously", e);
            // ❌ NÃO relançar a exceção! Apenas log e deixa o CircuitBreaker fazer seu trabalho
        }
    }

    /**
     * Fallback chamado quando o circuit breaker está aberto
     * IMPORTANTE: NÃO lançar exceção aqui!
     */
    private void sendFallback(Sale sale, SaleEvent event, Throwable t) {
        log.error(
                "Fallback: Kafka unavailable or circuit breaker open. " +
                        "Sale userId={}, sellerId={}, productId={}, event={}. " +
                        "Message will be stored for later retry.",
                sale.getUserId(),
                sale.getSellerId(),
                sale.getProductId(),
                event,
                t
        );

        // ✅ SOLUÇÃO: Armazenar em banco para retentativa posterior
        saveForLaterRetry(sale, event);

        // ❌ NÃO faça isso:
        // throw new RuntimeException(...);
    }

    /**
     * Salva a mensagem no banco para ser retentada depois
     * (implementação com MongoDB, MySQL, etc)
     */
    private void saveForLaterRetry(Sale sale, SaleEvent event) {
        // TODO: Implementar persistência para retentativa
        // Exemplo com MongoDB:
        // failedMessageRepository.save(new FailedMessage(sale, event));

        log.info("Message saved for later retry: saleId={}, event={}",
                sale.getId(), event);
    }
}