package com.vibranium.orchestrator.config.usecase;

import com.vibranium.orchestrator.adapters.out.SendSaleToTopicAdapter;
import com.vibranium.orchestrator.application.core.usecase.InventoryPreparedUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryPreparedConfig {

    @Bean
    public InventoryPreparedUseCase inventoryPreparedUseCase(
            SendSaleToTopicAdapter sendSaleToTopicAdapter
    ) {
        return new InventoryPreparedUseCase(sendSaleToTopicAdapter);
    }

}
