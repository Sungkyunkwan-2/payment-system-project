package com.paymentteamproject.common.dtos.auth;

import lombok.Getter;

@Getter
public class RegisterResponse {
    private final String username;
    private final String email;

    public RegisterResponse(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
