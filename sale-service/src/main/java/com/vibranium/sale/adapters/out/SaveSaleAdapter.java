package com.vibranium.sale.adapters.out;

import com.vibranium.sale.adapters.out.repository.SaleRepository;
import com.vibranium.sale.adapters.out.repository.mapper.SaleEntityMapper;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.ports.out.SaveSaleOutputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
public class SaveSaleAdapter implements SaveSaleOutputPort {

    private final SaleRepository saleRepository;
    private final SaleEntityMapper saleEntityMapper;

    public SaveSaleAdapter(SaleRepository saleRepository,
                           SaleEntityMapper saleEntityMapper) {
        this.saleRepository = saleRepository;
        this.saleEntityMapper = saleEntityMapper;
    }

    @Override
    @CircuitBreaker(name = "dbAccess", fallbackMethod = "saveFallback")
    public Sale save(Sale sale) {
        var saleEntity = saleEntityMapper.toSaleEntity(sale);
        var saleEntityResponse = saleRepository.save(saleEntity);
        return saleEntityMapper.toSale(saleEntityResponse);
    }

    /**
     * Fallback called when:
     * - the "dbAccess" circuit is OPEN, or
     * - a failure occurs that is counted by the Circuit Breaker.
     */
    private Sale saveFallback(Sale sale, Throwable t) {
        throw new RuntimeException(
                "Error saving sale. Database is unavailable or circuit breaker is open.",
                t
        );
    }
}