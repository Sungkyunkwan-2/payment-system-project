package com.paymentteamproject.domain.webhook.dto;

import lombok.Getter;

@Getter
public class BillingKeyPaymentResponse {

    private final String pgTxId;
    private final String paidAt;

    public BillingKeyPaymentResponse(String pgTxId, String paidAt) {
        this.pgTxId = pgTxId;
        this.paidAt = paidAt;
    }
}