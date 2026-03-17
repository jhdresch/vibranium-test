package com.vibranium.sale.config.log;

import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class ElasticsearchHttpAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private String url;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicBoolean elasticsearchAvailable = new AtomicBoolean(false);
    private final AtomicBoolean initializationChecked = new AtomicBoolean(false);

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected void append(ILoggingEvent event) {
        // NÃO tente enviar logs durante a inicialização do Spring
        if (!initializationChecked.get() &&
                (event.getLoggerName().contains("org.springframework") ||
                        event.getLoggerName().contains("org.hibernate") ||
                        event.getLoggerName().contains("org.apache"))) {
            return;
        }

        // Verifica se o Elasticsearch está disponível (apenas uma vez)
        if (!initializationChecked.get()) {
            checkElasticsearchAvailability();
            initializationChecked.set(true);
        }

        // Se não estiver disponível, não tenta enviar
        if (!elasticsearchAvailable.get()) {
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String logJson = String.format(
                    "{\"@timestamp\":\"%s\",\"level\":\"%s\",\"logger\":\"%s\",\"message\":\"%s\",\"thread\":\"%s\"}",
                    new java.util.Date(event.getTimeStamp()).toInstant().toString(),
                    event.getLevel().toString(),
                    event.getLoggerName(),
                    event.getFormattedMessage().replace("\"", "\\\""),
                    event.getThreadName()
            );

            HttpEntity<String> request = new HttpEntity<>(logJson, headers);
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            // Silently ignore errors
            elasticsearchAvailable.set(false);
        }
    }

    private void checkElasticsearchAvailability() {
        try {
            // Tenta resolver o hostname primeiro
            String host = extractHostFromUrl(url);

            // Tenta resolver o hostname
            InetAddress address = InetAddress.getByName(host);
            System.out.println("Elasticsearch host resolved to: " + address.getHostAddress());

            // Tenta uma requisição HEAD para verificar se o Elasticsearch está disponível
            String healthUrl = url.replace("/sale-service/_doc", "/_cluster/health");
            restTemplate.headForHeaders(healthUrl);
            elasticsearchAvailable.set(true);
            System.out.println("Elasticsearch is available at: " + url);
        } catch (Exception e) {
            elasticsearchAvailable.set(false);
            System.err.println("Elasticsearch not available: " + e.getMessage());
        }
    }

    private String extractHostFromUrl(String url) {
        // Remove protocolo
        String withoutProtocol = url.replace("http://", "").replace("https://", "");
        // Pega apenas o host (antes da primeira /)
        return withoutProtocol.split("/")[0];
    }
}