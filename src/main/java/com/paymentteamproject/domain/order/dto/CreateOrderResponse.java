package com.paymentteamproject.domain.order.dto;

import lombok.Getter;

@Getter
public class CreateOrderResponse {
    private final Long orderId;
    private final double totalAmount;
    private final Long orderNumber;

    public CreateOrderResponse(Long orderId, double totalAmount, Long orderNumber) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.orderNumber = orderNumber;
    }
}
