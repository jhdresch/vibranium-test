package com.vibranium.sale.config.usecase;

import com.vibranium.sale.adapters.out.SaveSaleAdapter;
import com.vibranium.sale.application.core.usecase.CancelSaleUseCase;
import com.vibranium.sale.application.core.usecase.FindSaleByIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CancelSaleConfig {

    @Bean
    public CancelSaleUseCase cancelSaleUseCase(
            FindSaleByIdUseCase findSaleByIdUseCase,
            SaveSaleAdapter saveSaleAdapter
    ) {
        return new CancelSaleUseCase(findSaleByIdUseCase, saveSaleAdapter);
    }

}
