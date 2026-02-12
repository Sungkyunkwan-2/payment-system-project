package com.paymentteamproject.domain.webhook.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BillingKeyPaymentRequest {
    private final String storeId;
    private final String billingKey;
    private final String orderName;
    private final String customerId;
    private final int totalAmount;
    private final String currency;
    private final String channelKey;
}