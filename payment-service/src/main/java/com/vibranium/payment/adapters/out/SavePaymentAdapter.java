package com.vibranium.payment.adapters.out;

import com.vibranium.payment.adapters.out.repository.PaymentRepository;
import com.vibranium.payment.adapters.out.repository.mapper.PaymentEntityMapper;
import com.vibranium.payment.application.core.domain.Payment;
import com.vibranium.payment.application.ports.out.SavePaymentOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
public class SavePaymentAdapter implements SavePaymentOutputPort {

    private final PaymentRepository paymentRepository;
    private final PaymentEntityMapper paymentEntityMapper;

    public SavePaymentAdapter(PaymentRepository paymentRepository,
                              PaymentEntityMapper paymentEntityMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentEntityMapper = paymentEntityMapper;
    }

    @Override
    @CircuitBreaker(name = "dbAccess", fallbackMethod = "saveFallback")
    public void save(Payment payment) {
        var paymentEntity = paymentEntityMapper.toPaymentEntity(payment);
        paymentRepository.save(paymentEntity);
    }

    /**
     * Fallback called when:
     * - the "dbAccess" circuit is OPEN, or
     * - a failure occurs that is counted by the Circuit Breaker.
     */
    private void saveFallback(Payment payment, Throwable t) {
        throw new RuntimeException(
                "Error saving payment. Database is unavailable or circuit breaker is open.",
                t
        );
    }
}