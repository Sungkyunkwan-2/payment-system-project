package com.paymentteamproject.domain.billing.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class BillingResponse {
    private final String billingKey;

    public BillingResponse(String billingKey) {
        this.billingKey = billingKey;

    }
}
