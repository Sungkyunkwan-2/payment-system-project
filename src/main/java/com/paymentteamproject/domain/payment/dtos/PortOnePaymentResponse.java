package com.paymentteamproject.domain.payment.dtos;

import lombok.Getter;

@Getter
public class PortOnePaymentResponse {
    private String id;  // paymentId
    private String status; // READY, PAID, CANCELLED, FAILED
    private double amount;
    private String requestedAt;
    private String paidAt;
}
