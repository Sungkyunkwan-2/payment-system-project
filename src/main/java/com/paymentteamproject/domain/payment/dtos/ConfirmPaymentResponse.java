package com.paymentteamproject.domain.payment.dtos;

import com.paymentteamproject.domain.payment.entity.PaymentStatus;
import lombok.Getter;

@Getter
public class ConfirmPaymentResponse {
    private final String orderId;
    private final PaymentStatus status;

    public ConfirmPaymentResponse(String orderId, PaymentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }
}
