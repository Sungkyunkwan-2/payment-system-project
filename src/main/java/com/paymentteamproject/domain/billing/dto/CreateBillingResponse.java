package com.paymentteamproject.domain.billing.dto;

import com.paymentteamproject.domain.billing.consts.BillingStatus;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateBillingResponse {
    private final String billingId;
    private final String paymentId;
    private final BigDecimal amount;
    private final BillingStatus status;

    public CreateBillingResponse(String billingId, String paymentId, BigDecimal amount, BillingStatus status) {
        this.billingId = billingId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
    }
}
