package com.vibranium.sale.application.ports.in;

import com.vibranium.sale.application.core.domain.Sale;

public interface CreateSaleInputPort {

    void create(Sale sale);

}
