package com.vibranium.payment.application.core.usecase;

import com.vibranium.payment.application.core.domain.Payment;
import com.vibranium.payment.application.core.domain.Sale;
import com.vibranium.payment.application.core.domain.enums.SaleEvent;
import com.vibranium.payment.application.core.domain.enums.TypeSale;
import com.vibranium.payment.application.ports.in.FindUserByIdInputPort;
import com.vibranium.payment.application.ports.in.SalePaymentInputPort;
import com.vibranium.payment.application.ports.out.SavePaymentOutputPort;
import com.vibranium.payment.application.ports.out.SendToKafkaOutputPort;
import com.vibranium.payment.application.ports.out.UpdateUserOutputPort;

import java.math.BigDecimal;

public class SalePaymentUseCase implements SalePaymentInputPort {

    private final FindUserByIdInputPort findUserByIdInputPort;
    private final UpdateUserOutputPort updateUserOutputPort;
    private final SavePaymentOutputPort savePaymentOutputPort;
    private final SendToKafkaOutputPort sendToKafkaOutputPort;

    public SalePaymentUseCase(
            FindUserByIdInputPort findUserByIdInputPort,
            UpdateUserOutputPort updateUserOutputPort,
            SavePaymentOutputPort savePaymentOutputPort,
            SendToKafkaOutputPort sendToKafkaOutputPort
    ) {
        this.findUserByIdInputPort = findUserByIdInputPort;
        this.updateUserOutputPort = updateUserOutputPort;
        this.savePaymentOutputPort = savePaymentOutputPort;
        this.sendToKafkaOutputPort = sendToKafkaOutputPort;
    }

    @Override
    public void payment(Sale sale) {
        try {
            if (sale.getType() == null) {
                throw new IllegalArgumentException("Sale type must not be null");
            }

            if (TypeSale.BUY.equals(sale.getType())) {
                processBuy(sale);
            } else if (TypeSale.SELL.equals(sale.getType())) {
                processSell(sale);
            } else {
                throw new IllegalArgumentException("Unsupported sale type: " + sale.getType());
            }

        } catch (Exception e) {
            // qualquer erro → fluxo de pagamento falhou
            sendToKafkaOutputPort.send(sale, SaleEvent.PAYMENT_FAILED);
        }
    }

    /**
     * Flow for BUY operations.
     * Buyer pays, seller receives.
     */
    private void processBuy(Sale sale) {
        var buyer = findUserByIdInputPort.find(sale.getUserId());
        if (buyer == null) {
            throw new RuntimeException("Buyer not found");
        }

        BigDecimal value = sale.getValue();
        if (buyer.getBalance().compareTo(value) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        buyer.debitBalance(value);

        var seller = findUserByIdInputPort.find(sale.getSellerId());
        if (seller == null) {
            throw new RuntimeException("The seller does not have a registered wallet");
        }

        seller.addBalance(value);

        updateUserOutputPort.update(buyer);
        updateUserOutputPort.update(seller);

        // se tudo deu certo, disparar evento de pagamento executado
        savePaymentOutputPort.save(buildPayment(sale));
        sendToKafkaOutputPort.send(sale, SaleEvent.PAYMENT_EXECUTED);
    }

    /**
     * Flow for SELL operations.
     * Here we enforce that the seller must have a wallet.
     */
    private void processSell(Sale sale) {
        var seller = findUserByIdInputPort.find(sale.getSellerId());
        if (seller == null) {
            // validação que você pediu, em inglês:
            throw new RuntimeException("The seller does not have a registered wallet");
        }

        sendToKafkaOutputPort.send(sale, SaleEvent.PAYMENT_EXECUTED);

    }

    private Payment buildPayment(Sale sale) {
        return new Payment(
                null,
                sale.getUserId(),
                sale.getSellerId(),
                sale.getOfferId(),
                sale.getValue()
        );
    }
}