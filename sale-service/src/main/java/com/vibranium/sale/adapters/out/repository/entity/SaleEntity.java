package com.vibranium.sale.adapters.out.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "sales")
public class SaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "offer_id")
    private Integer offerId;

    @Column(name = "type_sale_id")
    private Integer typeSaleId;

    private BigDecimal value;

    @Column(name = "status_id")
    private Integer statusId;

    private Integer quantity;

}
