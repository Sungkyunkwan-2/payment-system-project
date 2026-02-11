package com.paymentteamproject.domain.subscription.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateSubscriptionRequest {
    private String customerUid;
    private String planId;
    private String billingKey;
    private BigDecimal amount;
}
