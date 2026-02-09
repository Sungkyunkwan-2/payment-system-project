package com.paymentteamproject.domain.payment.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.order.entity.OrderStatus;
import com.paymentteamproject.domain.order.entity.Orders;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private double price;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime deletedAt;

    private Payment(Orders order, String paymentId, PaymentStatus status, double price) {
        this.order = order;
        this.paymentId = paymentId;
        this.status = status;
        this.price = price;
    }

    public static Payment start(Orders order, double price) {
        return new Payment(
                order,
                "PAY" + order.getId() + System.currentTimeMillis(),
                PaymentStatus.PENDING,
                price
        );
    }

    public Payment success() {
        Payment payment = new Payment(this.order, this.paymentId, PaymentStatus.SUCCESS, this.price);

        payment.paidAt = LocalDateTime.now();
        order.completedOrder(OrderStatus.ORDER_COMPLETED);

        return payment;
    }

    public Payment fail() {
        return new Payment(
                this.order, this.paymentId, PaymentStatus.FAILURE, this.price);
    }

    public void markRefunded() {
        if (this.status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("결제 성공 상태만 환불할 수 있습니다.");
        }
        this.status = PaymentStatus.REFUND;
        this.refundedAt = LocalDateTime.now();
    }
}
