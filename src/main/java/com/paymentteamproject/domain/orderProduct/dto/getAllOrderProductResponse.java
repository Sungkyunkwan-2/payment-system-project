package com.paymentteamproject.domain.orderProduct.dto;

import com.paymentteamproject.domain.order.consts.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class getAllOrderProductResponse {
    private final Long orderNumber;
    private final Long orderId;
    private final BigDecimal totalAmount;
    private final BigDecimal userPoints;
    private final BigDecimal finalAmount;
    private final BigDecimal earnedPoints;
    private final String currency;
    private final OrderStatus status;
    private final LocalDateTime createdAt;

    public getAllOrderProductResponse(Long orderNumber, Long orderId, BigDecimal totalAmount, BigDecimal userPoints, BigDecimal finalAmount, BigDecimal earnedPoints, String currency, OrderStatus status, LocalDateTime createdAt) {
        this.orderNumber = orderNumber;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.userPoints = userPoints;
        this.finalAmount = finalAmount;
        this.earnedPoints = earnedPoints;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }
}
