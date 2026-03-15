package com.vibranium.inventory.adapters.out;

import com.vibranium.inventory.adapters.out.repository.InventoryRepository;
import com.vibranium.inventory.adapters.out.repository.mapper.InventoryEntityMapper;
import com.vibranium.inventory.application.core.domain.Inventory;
import com.vibranium.inventory.application.ports.out.FindInventoryByProductIdOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindInventoryByProductIdOutputPortAdapter implements FindInventoryByProductIdOutputPort {

    private final InventoryRepository inventoryRepository;
    private final InventoryEntityMapper inventoryEntityMapper;

    public FindInventoryByProductIdOutputPortAdapter(InventoryRepository inventoryRepository,
                                                     InventoryEntityMapper inventoryEntityMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryEntityMapper = inventoryEntityMapper;
    }

    @Override
    @CircuitBreaker(name = "dbAccess", fallbackMethod = "findByProductIdAndSellerIdAndOfferIdFallback")
    public Optional<Inventory> findByProductIdAndSellerIdAndOfferId(Integer productId, Integer idOffer, Integer sellerId) {
        var inventoryEntity =
                inventoryRepository.findByProductIdAndSellerIdAndOfferId(productId, sellerId, idOffer);
        return inventoryEntity.map(inventoryEntityMapper::toInventory);
    }

    /**
     * Fallback called when:
     * - the "dbAccess" circuit is OPEN, or
     * - a failure occurs that is counted by the Circuit Breaker.
     */
    private Optional<Inventory> findByProductIdAndSellerIdAndOfferIdFallback(Integer productId,
                                                                             Integer idOffer,
                                                                             Integer sellerId,
                                                                             Throwable t) {
        throw new RuntimeException(
                String.format(
                        "Error fetching inventory for productId=%d, offerId=%d, sellerId=%d. " +
                                "Database is unavailable or circuit breaker is open.",
                        productId, idOffer, sellerId
                ),
                t
        );
    }
}