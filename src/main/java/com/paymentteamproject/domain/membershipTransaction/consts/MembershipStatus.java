package com.paymentteamproject.domain.membershipTransaction.consts;

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

    public static MembershipStatus getAvailableStatus(BigDecimal totalSpend) {

        if (totalSpend.compareTo(DIAMOND.minSpend) >= 0) return DIAMOND;
        if (totalSpend.compareTo(GOLD.minSpend) >= 0) return GOLD;
        if (totalSpend.compareTo(SILVER.minSpend) >= 0) return SILVER;
        return BRONZE;
    }

    public BigDecimal calculatePoint(BigDecimal paymentAmount) {
        return paymentAmount.multiply(this.ratio);
    }
}
