package com.vibranium.sale.application.ports.in;

import com.vibranium.sale.application.core.domain.Sale;

public interface CancelSaleInputPort {

    void cancel(Sale sale);

}
