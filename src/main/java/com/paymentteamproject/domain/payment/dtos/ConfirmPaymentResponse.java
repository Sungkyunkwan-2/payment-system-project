package com.paymentteamproject.domain.payment.dtos;

import com.paymentteamproject.domain.payment.entity.PaymentStatus;
import lombok.Getter;

@Getter
public class ConfirmPaymentResponse {
    private final Long orderId;
    private final PaymentStatus status;

    public ConfirmPaymentResponse(Long orderId, PaymentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }
}
