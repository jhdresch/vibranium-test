package com.vibranium.inventory.adapters.out;

import com.vibranium.inventory.adapters.out.repository.InventoryRepository;
import com.vibranium.inventory.adapters.out.repository.mapper.InventoryEntityMapper;
import com.vibranium.inventory.application.core.domain.Inventory;
import com.vibranium.inventory.application.ports.out.UpdateInventoryOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
public class UpdateInventoryAdapter implements UpdateInventoryOutputPort {

    private final InventoryRepository inventoryRepository;
    private final InventoryEntityMapper inventoryEntityMapper;

    public UpdateInventoryAdapter(InventoryRepository inventoryRepository,
                                  InventoryEntityMapper inventoryEntityMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryEntityMapper = inventoryEntityMapper;
    }

    @Override
    @CircuitBreaker(name = "dbAccess", fallbackMethod = "updateFallback")
    public void update(Inventory inventory) {
        var inventoryEntity = inventoryEntityMapper.toInventoryEntity(inventory);
        inventoryRepository.save(inventoryEntity);
    }

    /**
     * Fallback called when:
     * - the "dbAccess" circuit is OPEN, or
     * - a failure occurs that is counted by the Circuit Breaker.
     */
    private void updateFallback(Inventory inventory, Throwable t) {
        throw new RuntimeException(
                "Error updating inventory. Database is unavailable or circuit breaker is open.",
                t
        );
    }
}