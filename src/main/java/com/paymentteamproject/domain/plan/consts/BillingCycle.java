package com.paymentteamproject.domain.plan.consts;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public enum BillingCycle {
    MONTHLY(1, ChronoUnit.MONTHS),
    QUARTERLY(3, ChronoUnit.MONTHS),
    ANNUAL(1, ChronoUnit.YEARS);

    private final int period;
    private final ChronoUnit unit;

    BillingCycle(int period, ChronoUnit unit) {
        this.period = period;
        this.unit = unit;
    }

    public LocalDateTime calculatePeriodEnd(LocalDateTime from) {
        return from.plus(period, unit);
    }
}
