package com.vibranium.sale.application.ports.out;

import com.vibranium.sale.application.core.domain.Sale;

public interface SaveSaleOutputPort {

    Sale save(Sale sale);

}
