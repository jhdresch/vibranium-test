package com.vibranium.inventory.application.core.usecase;

import com.vibranium.inventory.application.core.domain.Inventory;
import com.vibranium.inventory.application.core.domain.Sale;
import com.vibranium.inventory.application.core.domain.enums.SaleEvent;
import com.vibranium.inventory.application.core.domain.enums.TypeSale;
import com.vibranium.inventory.application.ports.in.CreditInventoryInputPort;
import com.vibranium.inventory.application.ports.in.FindInventoryByProductIdAndIdOfferAndSellerIdInputPort;
import com.vibranium.inventory.application.ports.out.SendToKafkaOutputPort;
import com.vibranium.inventory.application.ports.out.UpdateInventoryOutputPort;

public class CreditInventorySellUseCase implements CreditInventoryInputPort {

    private final SendToKafkaOutputPort sendToKafkaOutputPort;
    private final FindInventoryByProductIdAndIdOfferAndSellerIdInputPort findInventoryByProductIdAndIdOfferAndSellerIdInputPort;
    private final UpdateInventoryOutputPort updateInventoryOutputPort;

    public CreditInventorySellUseCase(
            SendToKafkaOutputPort sendToKafkaOutputPort,
            FindInventoryByProductIdAndIdOfferAndSellerIdInputPort findInventoryByProductIdAndIdOfferAndSellerIdInputPort,
            UpdateInventoryOutputPort updateInventoryOutputPort
    ) {
        this.sendToKafkaOutputPort = sendToKafkaOutputPort;
        this.findInventoryByProductIdAndIdOfferAndSellerIdInputPort = findInventoryByProductIdAndIdOfferAndSellerIdInputPort;
        this.updateInventoryOutputPort = updateInventoryOutputPort;
    }

    @Override
    public void credit(Sale sale) {

        Inventory inventory = findInventoryByProductIdAndIdOfferAndSellerIdInputPort
                .find(sale.getProductId(), sale.getOfferId(), sale.getSellerId())
                .orElseGet(() -> criarNovoInventory(sale));

        updateInventoryOutputPort.update(inventory);

        sendToKafkaOutputPort.send(sale, SaleEvent.INCLUDED_INVENTORY);
    }

    private Inventory criarNovoInventory(Sale sale) {
        Inventory inventory = new Inventory();
        inventory.setSellerId(sale.getSellerId());
        inventory.setOfferPrice(sale.getValue());
        inventory.setQuantity(sale.getQuantity());
        inventory.setProductId(sale.getProductId());
        return inventory;
    }

    @Override
    public boolean getType(TypeSale typeSale) {
        return TypeSale.SELL.equals(typeSale);
    }
}
