package com.vibranium.sale.config.usecase;

import com.vibranium.sale.adapters.out.SaveSaleAdapter;
import com.vibranium.sale.adapters.out.SendCreatedSaleAdapter;
import com.vibranium.sale.application.core.usecase.CreateSaleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreateSaleConfig {

    @Bean
    public CreateSaleUseCase createSaleUseCase(
            SaveSaleAdapter saveSaleAdapter,
            SendCreatedSaleAdapter sendCreatedSaleAdapter
    ) {
        return new CreateSaleUseCase(saveSaleAdapter, sendCreatedSaleAdapter);
    }

}
