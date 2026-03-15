package com.vibranium.sale.adapters.in.controller;

import com.vibranium.sale.adapters.in.controller.mapper.SaleRequestMapper;
import com.vibranium.sale.adapters.in.controller.request.SaleRequest;
import com.vibranium.sale.application.core.domain.Sale;
import com.vibranium.sale.application.ports.in.CreateSaleInputPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class SaleController {

    private final CreateSaleInputPort createSaleInputPort;
    private final SaleRequestMapper saleRequestMapper;

    public SaleController(CreateSaleInputPort createSaleInputPort,
                          SaleRequestMapper saleRequestMapper) {
        this.createSaleInputPort = createSaleInputPort;
        this.saleRequestMapper = saleRequestMapper;
    }

    @PostMapping("/sales")
    @CircuitBreaker(name = "controllerExternalCall", fallbackMethod = "createSaleFallback")
    public ResponseEntity<Void> createSale(@Valid @RequestBody SaleRequest saleRequest) {
        log.info(
                "Received request to create sale. userId={}, sellerId={}, productId={}, offerId={}, value={}, quantity={}",
                saleRequest.getUserId(),
                saleRequest.getSellerId(),
                saleRequest.getProductId(),
                saleRequest.getOfferId(),
                saleRequest.getValue(),
                saleRequest.getQuantity()
        );

        Sale sale = saleRequestMapper.toSale(saleRequest);

        log.info(
                "Mapped SaleRequest to domain Sale. userId={}, sellerId={}, productId={}, offerId={}, value={}, quantity={}",
                sale.getUserId(),
                sale.getSellerId(),
                sale.getProductId(),
                sale.getOfferId(),
                sale.getValue(),
                sale.getQuantity()
        );

        createSaleInputPort.create(sale);

        log.info(
                "Sale created successfully. userId={}, sellerId={}, productId={}, offerId={}, value={}, quantity={}",
                sale.getUserId(),
                sale.getSellerId(),
                sale.getProductId(),
                sale.getOfferId(),
                sale.getValue(),
                sale.getQuantity()
        );

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Fallback chamado quando:
     * - O método createSale lança exceção suficiente para abrir/acionar o circuit breaker
     * - O circuito controllerExternalCall está OPEN
     */
    private ResponseEntity<Void> createSaleFallback(@Valid @RequestBody SaleRequest saleRequest,
                                                    Throwable t) {
        log.error(
                "Fallback triggered while creating sale. Circuit may be OPEN or an error occurred. " +
                        "userId={}, sellerId={}, productId={}, offerId={}, value={}, quantity={}",
                saleRequest.getUserId(),
                saleRequest.getSellerId(),
                saleRequest.getProductId(),
                saleRequest.getOfferId(),
                saleRequest.getValue(),
                saleRequest.getQuantity(),
                t
        );

        // Retorna 503 para indicar indisponibilidade temporária
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}