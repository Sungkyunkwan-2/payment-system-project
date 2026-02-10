package com.paymentteamproject.domain.masterMembership.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


//TODO: UserRank랑 겹침 - 우선 UserRank 우선 사용 중
@Getter
@RequiredArgsConstructor
public enum MembershipStatus {
    BRONZE(0.001),   // 0.1%
    SILVER(0.005),   // 0.5%
    GOLD(0.01),      // 1%
    DIAMOND(0.03);   // 3%

    private final double defaultRatio;
}
