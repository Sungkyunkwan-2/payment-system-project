package com.paymentteamproject.domain.pointTransactions.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name="point_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private double points;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type; //enum 구현 전

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean validity;

    private LocalDateTime deletedAt;

    public PointTransaction(User user, Order order, double points, TransactionType type, LocalDateTime expiresAt){
        this.user = user;
        this.order = order;
        this.points = points;
        this.type = type;
        this.expiresAt = expiresAt;
        this.validity = true;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
