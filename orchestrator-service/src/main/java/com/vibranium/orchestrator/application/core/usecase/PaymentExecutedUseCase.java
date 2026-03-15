package com.vibranium.orchestrator.application.core.usecase;

import com.vibranium.orchestrator.application.core.domain.Sale;
import com.vibranium.orchestrator.application.core.domain.enums.SaleEvent;
import com.vibranium.orchestrator.application.ports.in.WorkflowInputPort;
import com.vibranium.orchestrator.application.ports.out.SendSaleToTopicOutputPort;
import lombok.extern.slf4j.Slf4j;


public class PaymentExecutedUseCase implements WorkflowInputPort {

    private final SendSaleToTopicOutputPort sendSaleToTopicOutputPort;

    public PaymentExecutedUseCase(
            SendSaleToTopicOutputPort sendSaleToTopicOutputPort
    ) {
        this.sendSaleToTopicOutputPort = sendSaleToTopicOutputPort;
    }


    @Override
    public void execute(Sale sale) {

        sendSaleToTopicOutputPort.send(sale, SaleEvent.FINALIZE_SALE, "tp-saga-sale");

    }

    @Override
    public boolean isCalledByTheEvent(SaleEvent saleEvent) {
        return SaleEvent.PAYMENT_EXECUTED.equals(saleEvent);
    }

}
