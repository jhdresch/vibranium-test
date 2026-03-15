package com.vibranium.orchestrator.application.ports.in;

import com.vibranium.orchestrator.application.core.domain.Sale;
import com.vibranium.orchestrator.application.core.domain.enums.SaleEvent;

public interface WorkflowInputPort {

    void execute(Sale sale);

    boolean isCalledByTheEvent(SaleEvent saleEvent);

}
