package com.vibranium.sale.application.ports.in;

import com.vibranium.sale.application.core.domain.Sale;

public interface FindSaleByIdInputPort {

    Sale find(final Integer id);

}
