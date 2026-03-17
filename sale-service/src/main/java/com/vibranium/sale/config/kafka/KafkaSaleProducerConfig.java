package com.vibranium.sale.config.kafka;

import com.vibranium.sale.adapters.out.message.SaleMessage;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;


@Configuration
public class KafkaSaleProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String SPRING_KAFKA_BOOTSTRAP_SERVERS;

    @Bean
    public ProducerFactory<String, SaleMessage> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(BOOTSTRAP_SERVERS_CONFIG,SPRING_KAFKA_BOOTSTRAP_SERVERS);
        configProps.put(GROUP_ID_CONFIG, "sale");
        configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, CustomSerializer.class);
        // Retry do producer: 1 vez além da tentativa inicial
        configProps.put(RETRIES_CONFIG, 1);
        // Intervalo entre tentativas (ms)
        configProps.put(RETRY_BACKOFF_MS_CONFIG, 1000);
        // Opcional: acks/all para maior garantia
        configProps.put(ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, SaleMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}
