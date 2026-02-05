package com.paymentteamproject.domain.webhook.entity;

public enum PaymentWebhookPaymentStatus {
    READY,
    VIRTUAL_ACCOUNT_ISSUED,
    PAID,
    FAILED,
    PARTIAL_CANCELLED,
    CANCELLED,
    PAY_PENDING
}

