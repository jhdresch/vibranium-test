package com.vibranium.sale.adapters.out;

import com.vibranium.sale.adapters.out.repository.SaleRepository;
import com.vibranium.sale.adapters.out.repository.mapper.SaleEntityMapper;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.ports.out.FindSaleByIdOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FindSaleByIdAdapter implements FindSaleByIdOutputPort {

    private final SaleRepository saleRepository;
    private final SaleEntityMapper saleEntityMapper;

    public FindSaleByIdAdapter(SaleRepository saleRepository,
                               SaleEntityMapper saleEntityMapper) {
        this.saleRepository = saleRepository;
        this.saleEntityMapper = saleEntityMapper;
    }

    @Override
    @CircuitBreaker(name = "dbAccess", fallbackMethod = "findFallback")
    public Optional<Sale> find(Integer id) {
        var saleEntity = saleRepository.findById(id);
        return saleEntity.map(saleEntityMapper::toSale);
    }

    /**
     * Fallback called when:
     * - the "dbAccess" circuit is OPEN, or
     * - a failure occurs that is counted by the Circuit Breaker.
     */
    private Optional<Sale> findFallback(Integer id, Throwable t) {
        throw new RuntimeException(
                String.format("Error fetching sale with id %d. Database is unavailable or circuit breaker is open.", id),
                t
        );
    }
}