package com.paymentteamproject.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

// 유틸리티 클래스
public class AuthServiceSupport {
    public static Authentication validateRequest(AuthenticationManager authenticationManager, String email, String password){

        Authentication auth =  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // 토큰 생성을 위해 인증된 사용자 정보 추출
        if (auth.getPrincipal() == null) {
            throw new BadCredentialsException("Authentication principal is null");
        }

        return auth;
    }
}
