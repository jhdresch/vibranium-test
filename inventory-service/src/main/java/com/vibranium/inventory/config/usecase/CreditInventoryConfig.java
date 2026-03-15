package com.vibranium.inventory.config.usecase;

import com.vibranium.inventory.adapters.out.UpdateInventoryAdapter;
import com.vibranium.inventory.application.core.usecase.CreditInventoryBuyUseCase;
import com.vibranium.inventory.application.core.usecase.FindInventoryByProductIdAndIdOfferAndSellerIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditInventoryConfig {

    @Bean
    public CreditInventoryBuyUseCase creditInventoryUseCase(
            FindInventoryByProductIdAndIdOfferAndSellerIdUseCase findInventoryByProductIdUseCase,
            UpdateInventoryAdapter updateInventoryAdapter
    ) {
        return new CreditInventoryBuyUseCase(findInventoryByProductIdUseCase, updateInventoryAdapter);
    }

}
