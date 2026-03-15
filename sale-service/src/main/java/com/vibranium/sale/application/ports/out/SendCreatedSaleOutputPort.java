package com.vibranium.sale.application.ports.out;

import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.core.domain.enums.SaleEvent;

public interface SendCreatedSaleOutputPort {

    void send(Sale sale, SaleEvent event);

}
