package com.paymentteamproject.domain.pointTransaction.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private Orders order;

    @Column(nullable = false)
    private double points;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PointTransactionType type; //enum 구현 전

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean validity;

    private LocalDateTime deletedAt;

    @Builder
    public PointTransaction(User user, Orders order, double points, PointTransactionType type, LocalDateTime expiresAt){
        this.user = user;
        this.order = order;
        this.points = points;
        this.type = type;
        //TODO: 만료 시간 테스트로 3분으로 해놓음 - 수정 필요
        this.expiresAt = (expiresAt != null) ? expiresAt : LocalDateTime.now().plusMinutes(3);
        this.validity = true;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
