package com.paymentteamproject.domain.refund.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.refund.consts.RefundStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    private LocalDateTime refundedAt;

    private LocalDateTime deletedAt;

    public Refund(Payment payment, double amount, String reason) {
        this.payment = payment;
        this.amount = amount;
        this.reason = reason;
        this.status = RefundStatus.REQUEST;
        this.deletedAt = null;
    }

    public void markSuccess(LocalDateTime refundedAt) {
        this.status = RefundStatus.SUCCESS;
        this.refundedAt = refundedAt;
    }

    public void markFailure() {
        this.status = RefundStatus.FAILURE;
    }

    public boolean isSuccess() {
        return this.status == RefundStatus.SUCCESS;
    }

    public boolean isRequesting() {
        return this.status == RefundStatus.REQUEST;
    }
}
