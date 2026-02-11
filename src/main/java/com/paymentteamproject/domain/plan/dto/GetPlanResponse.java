package com.paymentteamproject.domain.plan.dto;

import com.paymentteamproject.domain.plan.consts.BillingCycle;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GetPlanResponse {
    private final String planId;
    private final String name;
    private final BigDecimal amount;
    private final BillingCycle billingCycle;

    public GetPlanResponse(String planId, String name, BigDecimal amount, BillingCycle billingCycle) {
        this.planId = planId;
        this.name = name;
        this.amount = amount;
        this.billingCycle = billingCycle;
    }
}
