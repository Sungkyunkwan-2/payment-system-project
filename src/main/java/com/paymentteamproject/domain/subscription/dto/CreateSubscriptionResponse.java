package com.paymentteamproject.domain.subscription.dto;

import lombok.Getter;

@Getter
public class CreateSubscriptionResponse {
    private final String subscriptionId;

    public CreateSubscriptionResponse(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
