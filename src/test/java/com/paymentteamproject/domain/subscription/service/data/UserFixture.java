package com.paymentteamproject.domain.subscription.service.data;

import com.paymentteamproject.domain.user.entity.User;

import java.math.BigDecimal;

public class UserFixture {
    public static final String DEFAULT_EMAIL = "test@example.com";
    public static final String DEFAULT_PHONE = "010-1111-1111";
    public static final String DEFAULT_USERNAME = "ravi";
    public static final String DEFAULT_PASSWORD = "123123123";
    public static final BigDecimal DEFAULT_POINTBALANCE = BigDecimal.ZERO;

    public static User createUser() {
        return new User(DEFAULT_EMAIL, DEFAULT_PHONE, DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_POINTBALANCE);
    }
}