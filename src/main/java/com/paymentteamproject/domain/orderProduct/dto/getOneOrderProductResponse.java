package com.paymentteamproject.domain.orderProduct.dto;

import com.paymentteamproject.domain.order.consts.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class getOneOrderProductResponse {
    private final Long orderNumber;
    private final Long orderId;
    private final double totalAmount;
    private final double userPoints;
    private final double finalAmount;
    private final double earnedPoints;
    private final String currency;
    private final OrderStatus status;
    private final LocalDateTime createdAt;

    public getOneOrderProductResponse(Long orderNumber, Long orderId, double totalAmount, double userPoints, double finalAmount, double earnedPoints, String currency, OrderStatus status, LocalDateTime createdAt) {
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
