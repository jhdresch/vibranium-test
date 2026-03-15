package com.vibranium.inventory.application.core.usecase;

import com.vibranium.inventory.application.core.domain.Inventory;
import com.vibranium.inventory.application.ports.in.FindInventoryByProductIdAndIdOfferAndSellerIdInputPort;
import com.vibranium.inventory.application.ports.out.FindInventoryByProductIdOutputPort;

import java.util.Optional;

public class FindInventoryByProductIdAndIdOfferAndSellerIdUseCase implements FindInventoryByProductIdAndIdOfferAndSellerIdInputPort {

    private final FindInventoryByProductIdOutputPort findInventoryByProductIdOutputPort;

    public FindInventoryByProductIdAndIdOfferAndSellerIdUseCase(
            FindInventoryByProductIdOutputPort findInventoryByProductIdOutputPort
    ) {
        this.findInventoryByProductIdOutputPort = findInventoryByProductIdOutputPort;
    }

    @Override
    public Optional<Inventory> find(Integer productId, Integer idOffer, Integer idSeller) {
        return findInventoryByProductIdOutputPort
                .findByProductIdAndSellerIdAndOfferId(productId, idOffer, idSeller);
    }

}
