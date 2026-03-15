package com.vibranium.inventory.config.usecase;

import com.vibranium.inventory.adapters.out.SendToKafkaAdapter;
import com.vibranium.inventory.adapters.out.UpdateInventoryAdapter;
import com.vibranium.inventory.application.core.usecase.CreditInventoryBuyUseCase;
import com.vibranium.inventory.application.core.usecase.CreditInventorySellUseCase;
import com.vibranium.inventory.application.core.usecase.FindInventoryByProductIdAndIdOfferAndSellerIdUseCase;
import com.vibranium.inventory.application.ports.in.CreditInventoryInputPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CreditInventoryWorkflowConfig {

    @Bean
    public CreditInventoryInputPort creditInventoryBuyUseCase(
            FindInventoryByProductIdAndIdOfferAndSellerIdUseCase findInventoryUseCase,
            UpdateInventoryAdapter updateInventoryAdapter
    ) {
        return new CreditInventoryBuyUseCase(
                findInventoryUseCase,
                updateInventoryAdapter
        );
    }

    @Bean
    public CreditInventoryInputPort creditInventorySellUseCase(
            SendToKafkaAdapter sendToKafkaAdapter,
            FindInventoryByProductIdAndIdOfferAndSellerIdUseCase findInventoryUseCase,
            UpdateInventoryAdapter updateInventoryAdapter
    ) {
        return new CreditInventorySellUseCase(
                sendToKafkaAdapter,
                findInventoryUseCase,
                updateInventoryAdapter
        );
    }
}
