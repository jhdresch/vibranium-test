package com.vibranium.orchestrator.config.usecase;

import com.vibranium.orchestrator.adapters.out.SendSaleToTopicAdapter;
import com.vibranium.orchestrator.application.core.usecase.CreatedSaleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreatedSaleConfig {

    @Bean
    public CreatedSaleUseCase createdSaleUseCase(SendSaleToTopicAdapter sendSaleToTopicAdapter) {
        return new CreatedSaleUseCase(sendSaleToTopicAdapter);
    }

}
