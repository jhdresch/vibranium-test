package com.vibranium.sale.config.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


public class ElasticsearchHttpAppender extends AppenderBase<ILoggingEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String url;

    @Value("${environment.ELASTICSEARCH_URL}")
    private String urlSpring;

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("@timestamp", Instant.ofEpochMilli(eventObject.getTimeStamp()).toString());
            doc.put("level", eventObject.getLevel().toString());
            doc.put("logger", eventObject.getLoggerName());
            doc.put("thread", eventObject.getThreadName());
            doc.put("message", eventObject.getFormattedMessage());

            // campos fixos úteis
            doc.put("service", "sale-service");
            doc.put("environment", "dev");

            byte[] json = objectMapper.writeValueAsBytes(doc);

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json);
            }

            int status = conn.getResponseCode();
            // se quiser debugar problemas de envio, pode tratar status >= 400 aqui

            conn.disconnect();
        } catch (Exception e) {
            // nunca use logger aqui pra não criar loop, só printa no stderr
            e.printStackTrace();
        }
    }
}