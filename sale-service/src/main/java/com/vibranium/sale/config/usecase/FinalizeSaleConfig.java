package com.vibranium.sale.config.usecase;

import com.vibranium.sale.adapters.out.SaveSaleAdapter;
import com.vibranium.sale.application.core.usecase.FinalizeSaleUseCase;
import com.vibranium.sale.application.core.usecase.FindSaleByIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinalizeSaleConfig {

    @Bean
    public FinalizeSaleUseCase finalizeSaleUseCase(
            FindSaleByIdUseCase findSaleByIdUseCase,
            SaveSaleAdapter saveSaleAdapter
    ) {
        return new FinalizeSaleUseCase(findSaleByIdUseCase, saveSaleAdapter);
    }

}
