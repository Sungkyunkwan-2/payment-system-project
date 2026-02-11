package com.paymentteamproject.domain.billing.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.billing.consts.BillingStatus;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "billings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Billing extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false)
    private String billingId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingStatus status;

    @Column(nullable = false)
    private String paymentId;

    @Column( nullable = false)
    private LocalDateTime attemptedAt;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column
    private String failureMessage;

    @Column
    private LocalDateTime deletedAt;

    public Billing(Subscription subscription, BigDecimal amount, BillingStatus status,
                   String paymentId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        this.subscription = subscription;
        this.amount = amount;
        this.status = status;
        this.paymentId = paymentId;
        this.attemptedAt = LocalDateTime.now();
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    @PrePersist
    private void generateBillingId() {
        if (this.billingId == null) {
            this.billingId = "BILL_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
