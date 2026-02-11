package com.paymentteamproject.domain.payment.event;

import com.paymentteamproject.domain.user.entity.User;
import java.math.BigDecimal;

public record TotalSpendChangedEvent(
        User user,
        BigDecimal delta
) {
}
