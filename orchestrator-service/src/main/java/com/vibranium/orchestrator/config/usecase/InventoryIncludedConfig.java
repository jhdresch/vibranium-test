package com.vibranium.orchestrator.config.usecase;

import com.vibranium.orchestrator.adapters.out.SendSaleToTopicAdapter;
import com.vibranium.orchestrator.application.core.usecase.InventoryIncludedUseCase;
import com.vibranium.orchestrator.application.ports.out.SendSaleToTopicOutputPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryIncludedConfig {

    @Bean
    public InventoryIncludedUseCase inventoryIncludedUseCase(SendSaleToTopicAdapter sendSaleToTopicAdapter) {
        return new InventoryIncludedUseCase(sendSaleToTopicAdapter);
    }
}
