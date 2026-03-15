package com.vibranium.payment.adapters.out;

import com.vibranium.payment.adapters.out.repository.UserRepository;
import com.vibranium.payment.adapters.out.repository.mapper.UserEntityMapper;
import com.vibranium.payment.application.core.domain.User;
import com.vibranium.payment.application.ports.out.UpdateUserOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
public class UpdateUserAdapter implements UpdateUserOutputPort {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    public UpdateUserAdapter(UserRepository userRepository,
                             UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    @CircuitBreaker(name = "dbAccess", fallbackMethod = "updateFallback")
    public void update(User user) {
        var userEntity = userEntityMapper.toUserEntity(user);
        userRepository.save(userEntity);
    }

    /**
     * Fallback called when:
     * - the "dbAccess" circuit is OPEN, or
     * - a failure occurs that is counted by the Circuit Breaker.
     */
    private void updateFallback(User user, Throwable t) {
        throw new RuntimeException(
                "Error updating user. Database is unavailable or circuit breaker is open.",
                t
        );
    }
}