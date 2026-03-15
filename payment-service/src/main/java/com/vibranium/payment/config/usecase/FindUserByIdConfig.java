package com.vibranium.payment.config.usecase;

import com.vibranium.payment.adapters.out.FindUserByIdAdapter;
import com.vibranium.payment.application.core.usecase.FindUserByIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FindUserByIdConfig {

    @Bean
    public FindUserByIdUseCase findUserByIdUseCase(
            FindUserByIdAdapter findUserByIdAdapter
    ) {
        return new FindUserByIdUseCase(findUserByIdAdapter);
    }

}
