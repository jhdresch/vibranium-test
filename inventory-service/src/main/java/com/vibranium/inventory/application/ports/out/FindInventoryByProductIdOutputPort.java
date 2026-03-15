package com.vibranium.inventory.application.ports.out;

import com.vibranium.inventory.application.core.domain.Inventory;

import java.util.Optional;

public interface FindInventoryByProductIdOutputPort {
    Optional<Inventory>  findByProductIdAndSellerIdAndOfferId(Integer productId, Integer idOffer, Integer idSeller);
}
