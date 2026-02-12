package com.paymentteamproject.domain.payment.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.payment.consts.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private BigDecimal price;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime deletedAt;

    private Payment(Orders order, String paymentId, PaymentStatus status, BigDecimal price) {
        this.order = order;
        this.paymentId = paymentId;
        this.status = status;
        this.price = price;
    }

    public static Payment start(Orders order, BigDecimal price) {
        return new Payment(
                order,
                "PAY_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8),
                PaymentStatus.PENDING,
                price
        );
    }

    public Payment success() {
        Payment payment = new Payment(this.order, this.paymentId, PaymentStatus.SUCCESS, this.price);

        payment.paidAt = LocalDateTime.now();
        order.completedOrder();

        return payment;
    }

    public Payment fail() {
        return new Payment(
                this.order, this.paymentId, PaymentStatus.FAILURE, this.price);
    }

    public Payment refund() {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("결제 성공 상태만 환불할 수 있습니다.");
        }
        Payment refunded = new Payment(this.order, this.paymentId, PaymentStatus.REFUND, this.price);
        refunded.paidAt = this.paidAt;
        refunded.refundedAt = LocalDateTime.now();
        return refunded;
    }

    public void updateStatus(PaymentStatus newStatus) {
        validateStatusTransition(newStatus);
        this.status = newStatus;
    }

    public void updatePaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public void updateRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    private void validateStatusTransition(PaymentStatus newStatus) {
        // 이미 환불된 결제는 다른 상태로 변경 불가
        if (this.status == PaymentStatus.REFUND) {
            throw new IllegalStateException(
                    String.format("환불된 결제는 상태를 변경할 수 없습니다. (현재: %s, 변경 시도: %s)",
                            this.status, newStatus)
            );
        }

        if (this.status == PaymentStatus.SUCCESS && newStatus == PaymentStatus.FAILURE) {
            throw new IllegalStateException(
                    String.format("성공한 결제를 실패로 변경할 수 없습니다. (현재: %s, 변경 시도: %s)",
                            this.status, newStatus)
            );
        }
    }
}
