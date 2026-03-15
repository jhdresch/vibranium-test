package com.vibranium.payment.application.ports.out;

import com.vibranium.payment.application.core.domain.Payment;

public interface SavePaymentOutputPort {

    void save(Payment payment);

}
