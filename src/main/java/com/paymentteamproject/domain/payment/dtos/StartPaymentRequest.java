package com.paymentteamproject.domain.payment.dtos;

import lombok.Getter;

@Getter
public class StartPaymentRequest {
    private Long orderId;
    private double totalAmount;
    private double pointsToUse;
}
