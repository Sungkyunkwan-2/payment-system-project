package com.paymentteamproject.domain.billing.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BillingStatus {
    COMPLETE("성공"),
    FAILED("실패");

    private final String description;
}
