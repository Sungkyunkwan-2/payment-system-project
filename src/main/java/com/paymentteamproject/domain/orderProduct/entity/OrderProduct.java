package com.paymentteamproject.domain.orderProduct.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.order.entity.Orders;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Long quantity;

    private LocalDateTime deletedAt;

    @Builder
    public OrderProduct(Orders order, Long productId, String productName, BigDecimal price, String currency, Long quantity) {
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.currency = currency;
        this.quantity = quantity;
    }
}
