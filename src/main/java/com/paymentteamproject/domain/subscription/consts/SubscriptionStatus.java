package com.paymentteamproject.domain.subscription.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionStatus {
    ACTIVE("활성"),
    CANCELLED("해지"),
    UNPAID("미납"),
    EXPIRED("기간 종료");

    private final String description;
}

