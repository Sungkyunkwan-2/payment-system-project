package com.paymentteamproject.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProfileResponse {
    private final String email;
    private final String customerUid;
    private final String name;
    private final String phone;
    private final BigDecimal pointBalance;

    @Builder
    public ProfileResponse(String email, String customerUid, String name, String phone, BigDecimal pointBalance) {
        this.email = email;
        this.customerUid = customerUid;
        this.name = name;
        this.phone = phone;
        this.pointBalance = pointBalance;
    }
}
