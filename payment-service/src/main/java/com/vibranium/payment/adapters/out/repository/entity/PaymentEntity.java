package com.vibranium.payment.adapters.out.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "buyer_id")
    private Integer buyerId;

    @Column(name = "selle_id")
    private Integer sellerId;

    @Column(name = "offer_id")
    private Integer offerId;

    @Column(name = "value")
    private BigDecimal value;
}
