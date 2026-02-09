package com.paymentteamproject.domain.user.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    // Getter에 의해 getAuthority()도 동일한 효과
    public String getValue() {
        return authority;
    }
}
