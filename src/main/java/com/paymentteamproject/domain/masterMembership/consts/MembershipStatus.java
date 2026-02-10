package com.paymentteamproject.domain.masterMembership.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@Getter
@RequiredArgsConstructor
public enum MembershipStatus {
    BRONZE(new BigDecimal(0.001), new BigDecimal(0), new BigDecimal(0)),   // 0.1%
    SILVER(new BigDecimal(0.005), new BigDecimal(5000000), new BigDecimal(1000)),   // 0.5%
    GOLD(new BigDecimal(0.01), new BigDecimal(10000000), new BigDecimal(2000)),      // 1%
    DIAMOND(new BigDecimal(0.03), new BigDecimal(50000000), new BigDecimal(3000));   // 3%

    private final BigDecimal ratio;
    private final BigDecimal minSpend;
    private final BigDecimal test;

    /**
     * 총 결제액에 따른 등급 판별
     */
    public static MembershipStatus getAvailableStatus(BigDecimal totalSpend) {
        // 높은 등급부터 비교합니다.
        // compareTo 결과: 1(크다), 0(같다), -1(작다) -> >= 이면 0 이상(>= 0)
        if (totalSpend.compareTo(DIAMOND.minSpend) >= 0) return DIAMOND;
        if (totalSpend.compareTo(GOLD.minSpend) >= 0) return GOLD;
        if (totalSpend.compareTo(SILVER.minSpend) >= 0) return SILVER;
        return BRONZE;
    }

    /**
     * 포인트 적립액 계산 (결제액 * 적립률)
     */
    public BigDecimal calculatePoint(BigDecimal paymentAmount) {
        // 보통 포인트는 소수점 이하를 절사(Floor)하거나 반올림하므로 설정이 필요할 수 있습니다.
        return paymentAmount.multiply(this.ratio);
    }
}
