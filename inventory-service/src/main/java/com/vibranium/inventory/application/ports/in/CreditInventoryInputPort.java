package com.vibranium.inventory.application.ports.in;

import com.vibranium.inventory.application.core.domain.Sale;
import com.vibranium.inventory.application.core.domain.enums.TypeSale;

public interface CreditInventoryInputPort {

    void credit(Sale sale);

    boolean getType(TypeSale typeSale);

}
