package com.paymentteamproject.domain.payment.dto;

import com.paymentteamproject.domain.payment.consts.PaymentStatus;
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
