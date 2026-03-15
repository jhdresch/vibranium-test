package com.vibranium.payment.application.ports.out;

import com.vibranium.payment.application.core.domain.Sale;
import com.vibranium.payment.application.core.domain.enums.SaleEvent;

public interface SendToKafkaOutputPort {

    void send(Sale sale, SaleEvent event);

}
