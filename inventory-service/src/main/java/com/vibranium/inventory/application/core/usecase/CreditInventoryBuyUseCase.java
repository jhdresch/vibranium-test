package com.vibranium.inventory.application.core.usecase;

import com.vibranium.inventory.application.core.domain.Inventory;
import com.vibranium.inventory.application.core.domain.Sale;
import com.vibranium.inventory.application.core.domain.enums.TypeSale;
import com.vibranium.inventory.application.ports.in.CreditInventoryInputPort;
import com.vibranium.inventory.application.ports.in.FindInventoryByProductIdAndIdOfferAndSellerIdInputPort;
import com.vibranium.inventory.application.ports.out.UpdateInventoryOutputPort;

public class CreditInventoryBuyUseCase implements CreditInventoryInputPort {

    private final FindInventoryByProductIdAndIdOfferAndSellerIdInputPort findInventoryByProductIdAndIdOfferAndSellerIdInputPort;
    private final UpdateInventoryOutputPort updateInventoryOutputPort;

    public CreditInventoryBuyUseCase(
            FindInventoryByProductIdAndIdOfferAndSellerIdInputPort findInventoryByProductIdAndIdOfferAndSellerIdInputPort,
            UpdateInventoryOutputPort updateInventoryOutputPort
    ) {
        this.findInventoryByProductIdAndIdOfferAndSellerIdInputPort = findInventoryByProductIdAndIdOfferAndSellerIdInputPort;
        this.updateInventoryOutputPort = updateInventoryOutputPort;
    }

    @Override
    public void credit(Sale sale) {

        Inventory inventory = findInventoryByProductIdAndIdOfferAndSellerIdInputPort
                .find(sale.getProductId(), sale.getOfferId(), sale.getSellerId())
                .orElseThrow(() ->
                        new RuntimeException("Estoque não encontrado para crédito da compra")
                );

        inventory.creditQuantity(sale.getQuantity());

        updateInventoryOutputPort.update(inventory);
    }

    @Override
    public boolean getType(TypeSale typeSale) {
        return TypeSale.BUY.equals(typeSale);
    }
}
