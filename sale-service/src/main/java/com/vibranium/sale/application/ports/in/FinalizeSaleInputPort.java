package com.vibranium.sale.application.ports.in;

import com.vibranium.sale.application.core.domain.Sale;

public interface FinalizeSaleInputPort {

    void finalize(Sale sale);

}
