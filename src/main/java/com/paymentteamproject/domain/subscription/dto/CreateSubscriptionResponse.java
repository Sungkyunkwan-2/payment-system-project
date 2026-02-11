package com.paymentteamproject.domain.subscription.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateSubscriptionResponse {
    private final Long id;
    private final String customerUid;
    private final String planId;
    private final String BillingKey;
    private final BigDecimal amount;

    public CreateSubscriptionResponse(Long id, String customerUid, String planId, String billingKey, BigDecimal amount) {
        this.id = id;
        this.customerUid = customerUid;
        this.planId = planId;
        this.BillingKey = billingKey;
        this.amount = amount;
    }
}
