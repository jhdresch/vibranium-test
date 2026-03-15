package com.vibranium.sale.config.kafka;

import com.vibranium.sale.adapters.out.message.SaleMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class CustomDeserializer implements Deserializer<SaleMessage> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SaleMessage deserialize(String topic, byte[] data) {
        try {
            if (data == null || data.length == 0) {
                return null;
            }
            return objectMapper.readValue(data, SaleMessage.class);
        } catch (Exception e) {
            System.err.println("Error when deserializing byte[] to SaleMessage. Topic=" + topic);
            e.printStackTrace();
            throw new SerializationException("Error when deserializing byte[] to SaleMessage", e);
        }
    }
}