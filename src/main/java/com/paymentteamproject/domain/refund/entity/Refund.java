package com.paymentteamproject.domain.refund.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.payment.entity.Payment;
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
        this.refundedAt = null;
        this.deletedAt = null;
    }

    private Refund(Payment payment, double amount, String reason, RefundStatus status, LocalDateTime refundedAt) {
        this.payment = payment;
        this.amount = amount;
        this.reason = reason;
        this.status = status;
        this.refundedAt = refundedAt;
        this.deletedAt = null;
    }

    // append-only: 성공 이벤트 row 생성
    public Refund success(LocalDateTime refundedAt) {
        return new Refund(this.payment, this.amount, this.reason, RefundStatus.SUCCESS, refundedAt);
    }

    // append-only: 실패 이벤트 row 생성
    public Refund failure() {
        return new Refund(this.payment, this.amount, this.reason, RefundStatus.FAILURE, null);
    }

    public boolean isSuccess() {
        return this.status == RefundStatus.SUCCESS;
    }

    public boolean isRequesting() {
        return this.status == RefundStatus.REQUEST;
    }

    public boolean isFailure() {
        return this.status == RefundStatus.FAILURE;
    }
}