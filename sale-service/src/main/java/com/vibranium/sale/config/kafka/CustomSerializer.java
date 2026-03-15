package com.vibranium.sale.config.kafka;

import com.vibranium.sale.adapters.out.message.SaleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class CustomSerializer implements Serializer<SaleMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, SaleMessage saleMessage) {
        try {
            if (saleMessage == null) {
                return null;
            }
            return objectMapper.writeValueAsBytes(saleMessage);
        } catch (Exception e) {
            e.printStackTrace(); // ajuda a ver o problema real
            throw new SerializationException("Error when serializing SaleMessage to byte[]", e);
        }
    }
}