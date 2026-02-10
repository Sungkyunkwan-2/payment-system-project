package com.paymentteamproject.domain.order.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateOrderResponse {
    private final Long orderId;
    private final BigDecimal totalAmount;
    private final Long orderNumber;

    public CreateOrderResponse(Long orderId, BigDecimal totalAmount, Long orderNumber) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.orderNumber = orderNumber;
    }
}
