package com.paymentteamproject.domain.webhook.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BillingKeyPaymentResponse {

    private final BillingKeyPaymentSummary payment;

    @Getter
    @RequiredArgsConstructor
    public static class BillingKeyPaymentSummary {
    private final String pgTxId;
    private final String paidAt;
    }
}