package com.paymentteamproject.domain.user.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRank {
    BRONZE(0.1, 0, 0),
    SILVER(0.5, 5_000_000, 1000),
    GOLD(1, 10_000_000, 2000),
    DIAMOND(3, 50_000_000, 3000)
    ;

    private final double ratio;
    private final double spend;
    private final double test;
}
