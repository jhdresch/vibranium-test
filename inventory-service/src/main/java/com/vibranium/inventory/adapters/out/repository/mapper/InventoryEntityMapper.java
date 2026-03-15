package com.vibranium.inventory.adapters.out.repository.mapper;

import com.vibranium.inventory.adapters.out.repository.entity.InventoryEntity;
import com.vibranium.inventory.application.core.domain.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryEntityMapper {

    Inventory toInventory(InventoryEntity inventoryEntity);

    InventoryEntity toInventoryEntity(Inventory inventory);

}
