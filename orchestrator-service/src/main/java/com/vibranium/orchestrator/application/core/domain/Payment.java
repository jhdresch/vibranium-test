package com.vibranium.orchestrator.application.core.domain;

import java.math.BigDecimal;

public class Payment {

    private Integer id;

    private Integer buyerId;

    private Integer sellerId;

    private Integer offerId;

    private BigDecimal value;

    public Payment() {
    }

    public Payment(Integer id, Integer buyerId, Integer sellerId, Integer offerId, BigDecimal value) {
        this.id = id;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.offerId = offerId;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Integer buyerId) {
        this.buyerId = buyerId;
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

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
