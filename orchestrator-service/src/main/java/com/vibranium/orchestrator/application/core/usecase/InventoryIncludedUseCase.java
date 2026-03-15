package com.vibranium.orchestrator.application.core.usecase;

import com.vibranium.orchestrator.application.core.domain.Sale;
import com.vibranium.orchestrator.application.core.domain.enums.SaleEvent;
import com.vibranium.orchestrator.application.ports.in.WorkflowInputPort;
import com.vibranium.orchestrator.application.ports.out.SendSaleToTopicOutputPort;


public class InventoryIncludedUseCase implements WorkflowInputPort {

    private final SendSaleToTopicOutputPort sendSaleToTopicOutputPort;

    public InventoryIncludedUseCase(
            SendSaleToTopicOutputPort sendSaleToTopicOutputPort
    ) {
        this.sendSaleToTopicOutputPort = sendSaleToTopicOutputPort;
    }

    @Override
    public void execute(Sale sale) {

        sendSaleToTopicOutputPort.send(sale, SaleEvent.EXECUTE_PAYMENT, "tp-saga-payment");

    }

    @Override
    public boolean isCalledByTheEvent(SaleEvent saleEvent) {
        return SaleEvent.INCLUDED_INVENTORY.equals(saleEvent);
    }



}
