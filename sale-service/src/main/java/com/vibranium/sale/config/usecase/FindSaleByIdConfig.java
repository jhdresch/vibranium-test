package com.vibranium.sale.config.usecase;

import com.vibranium.sale.adapters.out.FindSaleByIdAdapter;
import com.vibranium.sale.application.core.usecase.FindSaleByIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FindSaleByIdConfig {

    @Bean
    public FindSaleByIdUseCase findSaleByIdUseCase(
            FindSaleByIdAdapter findSaleByIdAdapter
    ) {
        return new FindSaleByIdUseCase(findSaleByIdAdapter);
    }

}
