package com.paymentteamproject.domain.webhook.dto;

import lombok.Data;
import lombok.Getter;

@Getter
public class BillingKeyPaymentResponse {
    private String paymentId;
    private String status;
    private String failureReason;
    private PaymentAmount amount;
    private String paidAt;

    @Data
    public static class PaymentAmount {
        private int total;
        private int taxFree;
        private int vat;
    }
}