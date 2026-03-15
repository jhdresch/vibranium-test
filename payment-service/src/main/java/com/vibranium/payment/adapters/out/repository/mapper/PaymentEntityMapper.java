package com.vibranium.payment.adapters.out.repository.mapper;

import com.vibranium.payment.adapters.out.repository.entity.PaymentEntity;
import com.vibranium.payment.application.core.domain.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentEntityMapper {

    PaymentEntity toPaymentEntity(Payment payment);

}
