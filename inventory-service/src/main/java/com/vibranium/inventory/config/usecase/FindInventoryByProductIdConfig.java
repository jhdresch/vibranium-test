package com.vibranium.inventory.config.usecase;

import com.vibranium.inventory.adapters.out.FindInventoryByProductIdOutputPortAdapter;
import com.vibranium.inventory.application.core.usecase.FindInventoryByProductIdAndIdOfferAndSellerIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FindInventoryByProductIdConfig {

    @Bean
    public FindInventoryByProductIdAndIdOfferAndSellerIdUseCase findInventoryByProductIdUseCase(
            FindInventoryByProductIdOutputPortAdapter findInventoryByProductIdOutputPortAdapter
    ) {
        return new FindInventoryByProductIdAndIdOfferAndSellerIdUseCase(findInventoryByProductIdOutputPortAdapter);
    }

}
