package com.paymentteamproject.domain.point_transactions.entity;

import com.paymentteamproject.common.entity.BaseEntity;
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

    private Long userId;

    private Long orderId;

    private double points;

    private TransactionType type; //enum 구현 전

    private LocalDateTime expiresAt;

    private boolean validity;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public PointTransaction(Long userId, Long orderId, double points, TransactionType type, LocalDateTime expiresAt){
        this.userId = userId;
        this.orderId = orderId;
        this.points = points;
        this.type = type;
        this.expiresAt = expiresAt;
        this.validity = true;
    }
}
