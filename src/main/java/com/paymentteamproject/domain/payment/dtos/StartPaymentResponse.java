package com.paymentteamproject.domain.payment.dtos;

import com.paymentteamproject.domain.payment.entity.PaymentStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StartPaymentResponse {
    private final String paymentId;
    private final PaymentStatus status;
    private final LocalDateTime createdAt;

    public StartPaymentResponse(String paymentId, PaymentStatus status, LocalDateTime createdAt) {
        this.paymentId = paymentId;
        this.status = status;
        this.createdAt = createdAt;
    }
}
