package com.paymentteamproject.domain.billing.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateBillingRequest {
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}
