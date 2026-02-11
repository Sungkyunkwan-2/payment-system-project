package com.paymentteamproject.domain.subscription.dto;

import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GetSubscriptionResponse {
    private final String subscriptionId;
    private final String customerUid;
    private final String planId;
    private final String paymentMethodId;
    private final SubscriptionStatus status;
    private final BigDecimal amount;
    private final LocalDateTime currentPeriodEnd;

    public GetSubscriptionResponse(String subscriptionId, String customerUid, String planId, String paymentMethodId, SubscriptionStatus status, BigDecimal amount, LocalDateTime currentPeriodEnd) {
        this.subscriptionId = subscriptionId;
        this.customerUid = customerUid;
        this.planId = planId;
        this.paymentMethodId = paymentMethodId;
        this.status = status;
        this.amount = amount;
        this.currentPeriodEnd = currentPeriodEnd;
    }
}
