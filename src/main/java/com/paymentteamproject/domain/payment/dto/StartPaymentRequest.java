package com.paymentteamproject.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StartPaymentRequest {
    @NotNull(message = "id는 필수 값 입니다.")
    private Long orderId;
    @NotNull(message = "총금액은 필수 값 입니다.")
    private BigDecimal totalAmount;
    private BigDecimal pointsToUse;
}
