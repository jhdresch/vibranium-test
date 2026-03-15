package com.vibranium.payment.application.core.domain;


import com.vibranium.payment.application.core.domain.enums.SaleStatus;
import com.vibranium.payment.application.core.domain.enums.TypeSale;

import java.math.BigDecimal;

public class Sale {

    private Integer id;

    private Integer productId;

    private Integer userId;

    private Integer sellerId;

    private Integer offerId;

    private BigDecimal value;

    private SaleStatus status;

    private TypeSale type;

    private Integer quantity;

    public Sale() {
    }

    public Sale(Integer id, Integer productId, Integer userId, Integer sellerId, Integer offerId, BigDecimal value, SaleStatus status, TypeSale type, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.sellerId = sellerId;
        this.offerId = offerId;
        this.value = value;
        this.status = status;
        this.type = type;
        this.quantity = quantity;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSellerId() {
        return sellerId;
    }

    public void setSellerId(Integer sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getOfferId() {
        return offerId;
    }

    public void setOfferId(Integer offerId) {
        this.offerId = offerId;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public TypeSale getType() {
        return type;
    }

    public void setType(TypeSale type) {
        this.type = type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}