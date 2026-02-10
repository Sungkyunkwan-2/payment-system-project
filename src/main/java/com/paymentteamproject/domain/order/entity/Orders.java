package com.paymentteamproject.domain.order.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.order.consts.OrderStatus;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private BigDecimal totalPrice;

    private BigDecimal usedPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime deletedAt;

    @Builder
    public Orders(User user, Long orderNumber, BigDecimal totalPrice, BigDecimal usedPoint, OrderStatus status) {
        this.user = user;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.usedPoint = usedPoint;
        this.status = status;
    }

    //주문 생성 시 주문 번호 자동 생성
    @PrePersist
    private void generateOrderNumber() {
        if (this.orderNumber == null) {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            this.orderNumber = Long.parseLong("1" + timestamp);
        }
    }

    public void completedOrder() {
        this.status = OrderStatus.ORDER_COMPLETED;
    }
    public void markRefunded() {
        // 환불 가능한 주문 상태만 허용하고 싶으면 체크
        if (this.status != OrderStatus.ORDER_COMPLETED) {
            throw new IllegalStateException("주문 완료 상태만 환불할 수 있습니다.");
        }
        this.status = OrderStatus.ORDER_CANCELED;
    }

    public void updateStatus(OrderStatus newStatus) {
        validateStatusTransition(newStatus);
        this.status = newStatus;
    }

    private void validateStatusTransition(OrderStatus newStatus) {
        // 이미 취소된 주문은 완료로 변경 불가
        if (this.status == OrderStatus.ORDER_CANCELED && newStatus == OrderStatus.ORDER_COMPLETED) {
            throw new IllegalStateException(
                    String.format("취소된 주문은 완료로 변경할 수 없습니다. (현재: %s, 변경 시도: %s)",
                            this.status, newStatus)
            );
        }
    }
}
