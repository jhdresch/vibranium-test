package com.vibranium.payment.adapters.out;

import com.vibranium.payment.adapters.out.repository.UserRepository;
import com.vibranium.payment.adapters.out.repository.mapper.UserEntityMapper;
import com.vibranium.payment.application.core.domain.User;
import com.vibranium.payment.application.ports.out.FindUserByIdOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindUserByIdAdapter implements FindUserByIdOutputPort {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;

    public FindUserByIdAdapter(UserRepository userRepository,
                               UserEntityMapper userEntityMapper) {
        this.userRepository = userRepository;
        this.userEntityMapper = userEntityMapper;
    }

    @Override
    @CircuitBreaker(name = "dbAccess", fallbackMethod = "findFallback")
    public Optional<User> find(Integer userId) {
        var userEntity = userRepository.findById(userId);
        return userEntity.map(userEntityMapper::toUser);
    }

    /**
     * Fallback called when:
     * - the "dbAccess" circuit is OPEN, or
     * - a failure occurs that is counted by the Circuit Breaker.
     */
    private Optional<User> findFallback(Integer userId, Throwable t) {
        throw new RuntimeException(
                String.format("Error fetching user with id %d. Database is unavailable or circuit breaker is open.", userId),
                t
        );
    }
}