package com.paymentteamproject.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegisterRequest {
    @NotBlank(message = "사용자명은 필수 입력 항목입니다.")
    @Size(min = 2, message = "사용자명은 2자 이상이어야 합니다.")
    private String name;
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;
    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-xxxx-xxxx 형식이어야 합니다.")
    private String phone;

    @Builder
    private RegisterRequest(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }
}
