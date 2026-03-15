package com.vibranium.inventory.application.core.usecase;

import com.vibranium.inventory.application.core.domain.Inventory;
import com.vibranium.inventory.application.core.domain.Sale;
import com.vibranium.inventory.application.core.domain.enums.SaleEvent;
import com.vibranium.inventory.application.ports.in.DebitInventoryInputPort;
import com.vibranium.inventory.application.ports.in.FindInventoryByProductIdAndIdOfferAndSellerIdInputPort;
import com.vibranium.inventory.application.ports.out.SendToKafkaOutputPort;
import com.vibranium.inventory.application.ports.out.UpdateInventoryOutputPort;


import java.util.Objects;


public class DebitInventoryUseCase implements DebitInventoryInputPort {

    private final FindInventoryByProductIdAndIdOfferAndSellerIdInputPort findInventoryByProductIdAndIdOfferAndSellerIdInputPort;

    private final UpdateInventoryOutputPort updateInventoryOutputPort;

    private final SendToKafkaOutputPort sendToKafkaOutputPort;

    public DebitInventoryUseCase(
            FindInventoryByProductIdAndIdOfferAndSellerIdInputPort findInventoryByProductIdAndIdOfferAndSellerIdInputPort,
            UpdateInventoryOutputPort updateInventoryOutputPort,
            SendToKafkaOutputPort sendToKafkaOutputPort
    ) {
        this.findInventoryByProductIdAndIdOfferAndSellerIdInputPort = findInventoryByProductIdAndIdOfferAndSellerIdInputPort;
        this.updateInventoryOutputPort = updateInventoryOutputPort;
        this.sendToKafkaOutputPort = sendToKafkaOutputPort;
    }

    @Override
    public void debit(Sale sale) {
        try {

            Inventory inventory = findInventoryByProductIdAndIdOfferAndSellerIdInputPort
                    .find(sale.getProductId(), sale.getOfferId(), sale.getSellerId())
                    .orElseThrow(() -> new RuntimeException("Estoque não encontrado para este produto!"));

            if (!Objects.equals(inventory.getQuantity(), sale.getQuantity())) {
                throw new RuntimeException("Quantidade do estoque diferente da quantidade da venda");
            }

            if (!Objects.equals(inventory.getOfferPrice(), sale.getValue())) {
                throw new RuntimeException("Valor da oferta diferente da oferta.");
            }

            inventory.debitQuantity(sale.getQuantity());

            updateInventoryOutputPort.update(inventory);

            sendToKafkaOutputPort.send(sale, SaleEvent.INVENTORY_PREPARED);

        } catch (Exception e) {
            sendToKafkaOutputPort.send(sale, SaleEvent.INVENTORY_ERROR);
        }
    }
}


