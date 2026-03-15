package com.vibranium.inventory.application.ports.in;

import com.vibranium.inventory.application.core.domain.Sale;

public interface DebitInventoryInputPort {

    void debit(Sale sale);

}
