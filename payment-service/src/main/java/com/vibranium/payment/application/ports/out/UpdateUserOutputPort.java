package com.vibranium.payment.application.ports.out;

import com.vibranium.payment.application.core.domain.User;

public interface UpdateUserOutputPort {

    void update(User user);

}
