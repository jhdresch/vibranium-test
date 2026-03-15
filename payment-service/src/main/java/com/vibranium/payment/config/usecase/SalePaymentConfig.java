package com.vibranium.payment.config.usecase;

import com.vibranium.payment.adapters.out.SavePaymentAdapter;
import com.vibranium.payment.adapters.out.SendToKafkaAdapter;
import com.vibranium.payment.adapters.out.UpdateUserAdapter;
import com.vibranium.payment.application.core.usecase.FindUserByIdUseCase;
import com.vibranium.payment.application.core.usecase.SalePaymentUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SalePaymentConfig {

    @Bean
    public SalePaymentUseCase salePaymentUseCase(
            FindUserByIdUseCase findUserByIdUseCase,
            UpdateUserAdapter updateUserAdapter,
            SavePaymentAdapter savePaymentAdapter,
            SendToKafkaAdapter sendToKafkaAdapter
    ) {
        return new SalePaymentUseCase(findUserByIdUseCase, updateUserAdapter, savePaymentAdapter, sendToKafkaAdapter);
    }

}
