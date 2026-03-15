package com.vibranium.sale.adapters.in.controller.request;

import com.vibranium.sale.application.core.domain.enums.TypeSale;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {

    @NotNull
    private Integer userId;

    @NotNull
    private Integer sellerId;

    @NotNull
    private Integer offerId;

    @NotNull
    private Integer productId;

    @NotNull
    private Integer quantity;

    @NotNull
    private BigDecimal value;

    @NotNull
    private Integer type;

}
