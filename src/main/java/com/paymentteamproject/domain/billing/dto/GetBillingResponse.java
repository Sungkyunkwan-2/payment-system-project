package com.paymentteamproject.domain.billing.dto;

import com.paymentteamproject.domain.billing.consts.BillingStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GetBillingResponse {
    private final String billingId;
    private final LocalDateTime periodStart;
    private final LocalDateTime periodEnd;
    private final BigDecimal amount;
    private final BillingStatus status;
    private final String paymentId;
    private final LocalDateTime attemptDate;
    private final String failureMessage;

    public GetBillingResponse(String billingId, LocalDateTime periodStart, LocalDateTime periodEnd, BigDecimal amount, BillingStatus status, String paymentId, LocalDateTime attemptDate, String failureMessage) {
        this.billingId = billingId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.amount = amount;
        this.status = status;
        this.paymentId = paymentId;
        this.attemptDate = attemptDate;
        this.failureMessage = failureMessage;
    }
}