package com.paymentteamproject.domain.plan.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.plan.consts.BillingCycle;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "plans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String planId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    private LocalDateTime deletedAt;

    public Plan(String planId, String name, BigDecimal price, BillingCycle billingCycle) {
        this.planId = planId;
        this.name = name;
        this.price = price;
        this.billingCycle = billingCycle;
    }
}
