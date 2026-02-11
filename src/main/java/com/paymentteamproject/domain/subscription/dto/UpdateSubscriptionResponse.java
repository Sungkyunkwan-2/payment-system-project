package com.paymentteamproject.domain.subscription.dto;

import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import lombok.Getter;

@Getter
public class UpdateSubscriptionResponse {
    private final String subscriptionId;
    private final SubscriptionStatus status;

    public UpdateSubscriptionResponse(String subscriptionId, SubscriptionStatus status) {
        this.subscriptionId = subscriptionId;
        this.status = status;
    }
}
