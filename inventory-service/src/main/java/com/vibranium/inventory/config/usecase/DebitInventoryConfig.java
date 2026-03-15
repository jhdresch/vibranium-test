package com.vibranium.inventory.config.usecase;

import com.vibranium.inventory.adapters.out.SendToKafkaAdapter;
import com.vibranium.inventory.adapters.out.UpdateInventoryAdapter;
import com.vibranium.inventory.application.core.usecase.DebitInventoryUseCase;
import com.vibranium.inventory.application.core.usecase.FindInventoryByProductIdAndIdOfferAndSellerIdUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebitInventoryConfig {

    @Bean
    public DebitInventoryUseCase debitInventoryUseCase(
            FindInventoryByProductIdAndIdOfferAndSellerIdUseCase findInventoryByProductIdUseCase,
            UpdateInventoryAdapter updateInventoryAdapter,
            SendToKafkaAdapter sendToKafkaAdapter
    ) {
        return new DebitInventoryUseCase(findInventoryByProductIdUseCase, updateInventoryAdapter, sendToKafkaAdapter);
    }

}
