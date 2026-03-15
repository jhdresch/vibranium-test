package com.vibranium.inventory.application.ports.out;

import com.vibranium.inventory.application.core.domain.Inventory;

public interface UpdateInventoryOutputPort {

    void update(Inventory inventory);

}
