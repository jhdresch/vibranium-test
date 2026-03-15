package com.vibranium.orchestrator.config.usecase;

import com.vibranium.orchestrator.adapters.out.SendSaleToTopicAdapter;
import com.vibranium.orchestrator.application.core.usecase.PaymentExecutedUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentExecutedConfig {

    @Bean
    public PaymentExecutedUseCase paymentExecutedUseCase(
            SendSaleToTopicAdapter sendSaleToTopicAdapter
    ) {
        return new PaymentExecutedUseCase(sendSaleToTopicAdapter);
    }

}