package com.paymentteamproject.common.dtos.auth;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String name;
    private final String email;


    public LoginResponse(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
