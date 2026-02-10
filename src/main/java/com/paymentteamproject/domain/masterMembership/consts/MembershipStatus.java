package com.paymentteamproject.domain.masterMembership.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum MembershipStatus {
    BRONZE(0.001, 0, 0),   // 0.1%
    SILVER(0.005, 5000000, 1000),   // 0.5%
    GOLD(0.01, 10000000, 2000),      // 1%
    DIAMOND(0.03, 50000000, 3000);   // 3%

    private final double ratio;
    private final double spend;
    private final double test;
}
