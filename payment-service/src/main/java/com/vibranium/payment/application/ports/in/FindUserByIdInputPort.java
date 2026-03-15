package com.vibranium.payment.application.ports.in;

import com.vibranium.payment.application.core.domain.User;

public interface FindUserByIdInputPort {

    User find(final Integer id);

}
