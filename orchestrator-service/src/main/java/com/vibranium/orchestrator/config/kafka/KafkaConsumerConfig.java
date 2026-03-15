package com.vibranium.orchestrator.config.kafka;

import com.vibranium.orchestrator.adapters.out.message.SaleMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRY_BACKOFF_MS_CONFIG;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {


    @Value("${environment.KAFKA_BOOTSTRAP_SERVERS}")
    private String KAFKA_BOOTSTRAP_SERVERS;

    @Bean
    public ConsumerFactory<String, SaleMessage> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, CustomDeserializer.class);
        props.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Retry do producer: 1 vez além da tentativa inicial
        props.put(RETRIES_CONFIG, 1);
        // Intervalo entre tentativas (ms)
        props.put(RETRY_BACKOFF_MS_CONFIG, 1000);
        // Opcional: acks/all para maior garantia
        props.put(ACKS_CONFIG, "all");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SaleMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SaleMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public CommonErrorHandler commonErrorHandler() {
        FixedBackOff backOff = new FixedBackOff(1000L, 1L); // 1 retry

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);

        return errorHandler;
    }


}
