package com.paymentteamproject.domain.subscription.service.data;

import com.paymentteamproject.domain.plan.consts.BillingCycle;
import com.paymentteamproject.domain.plan.entity.Plan;

import java.math.BigDecimal;

public class PlanFixture {
    public static final String DEFAULT_PLAN_ID = "plan-123";
    public static final String DEFAULT_NAME = "BASIC Plan";
    public static final BigDecimal DEFAULT_AMOUNT = BigDecimal.valueOf(10000L);
    public static final BillingCycle DEFAULT_BILLING_CYCLE = BillingCycle.ANNUAL;

    public static Plan createPlan() {
        return new Plan(DEFAULT_PLAN_ID, DEFAULT_NAME, DEFAULT_AMOUNT, DEFAULT_BILLING_CYCLE);
    }
}