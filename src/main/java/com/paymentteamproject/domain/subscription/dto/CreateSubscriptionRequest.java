package com.paymentteamproject.domain.subscription.dto;

import lombok.Getter;

@Getter
public class CreateSubscriptionRequest {
    private Long userId;
    private String planId;
    private Long paymentMethodId;
}
