package com.paymentteamproject.domain.order.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private Long orderNumber;

    @Column(nullable = false)
    private double totalPrice;

    private double usedPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime deletedAt;

    @Builder
    public Orders(User user, Long orderNumber, double totalPrice, double usedPoint, OrderStatus status) {
        this.user = user;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.usedPoint = usedPoint;
        this.status = status;
    }
}
