package com.vibranium.orchestrator.config.usecase;

import com.vibranium.orchestrator.adapters.out.SendSaleToTopicAdapter;
import com.vibranium.orchestrator.application.core.usecase.InventoryFailureUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryFailureConfig {

    @Bean
    public InventoryFailureUseCase inventoryFailureUseCase(
            SendSaleToTopicAdapter sendSaleToTopicAdapter
    ) {
        return new InventoryFailureUseCase(sendSaleToTopicAdapter);
    }

}