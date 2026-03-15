package com.vibranium.inventory.application.ports.in;

import com.vibranium.inventory.application.core.domain.Inventory;

import java.util.Optional;

public interface FindInventoryByProductIdAndIdOfferAndSellerIdInputPort {

    Optional<Inventory> find(Integer productId, Integer idOffer, Integer sellerId);

}
