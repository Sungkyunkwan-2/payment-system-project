package com.paymentteamproject.common.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotBlank(message = "공백일 수 없습니다.")
    private String email;
    @NotBlank(message = "공백일 수 없습니다.")
    private String password;
}
