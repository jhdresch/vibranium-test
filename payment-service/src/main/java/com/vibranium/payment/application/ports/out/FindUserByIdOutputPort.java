package com.vibranium.payment.application.ports.out;

import com.vibranium.payment.application.core.domain.User;

import java.util.Optional;

public interface FindUserByIdOutputPort {

    Optional<User> find(Integer userId);

}
