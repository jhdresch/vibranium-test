package com.vibranium.payment.application.ports.in;

import com.vibranium.payment.application.core.domain.Sale;

public interface SalePaymentInputPort {

    void payment(Sale sale);

}
