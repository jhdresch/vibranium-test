package com.vibranium.orchestrator.application.ports.out;

import com.vibranium.orchestrator.application.core.domain.Sale;
import com.vibranium.orchestrator.application.core.domain.enums.SaleEvent;

public interface SendSaleToTopicOutputPort {

    void send(Sale sale, SaleEvent saleEvent, String topic);

}
