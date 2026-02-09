package com.paymentteamproject.domain.auth.service;

import com.paymentteamproject.domain.auth.dto.LoginRequest;
import com.paymentteamproject.security.CustomUserDetails;
import com.paymentteamproject.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public String login(LoginRequest request) throws AuthenticationException {

        // 1. 이메일/비밀번호 검증
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // 토큰 생성을 위해 인증된 사용자 정보 추출
        if (auth.getPrincipal() == null) {
            throw new IllegalStateException("Authentication principal is null");
        }

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        // 2. 인증 성공 시 JWT 토큰 생성
        return jwtTokenProvider.createToken(
                userDetails.getUsername(), // email
                userDetails.getName(), // username
                userDetails.getRoleAuthority() // role
        );
    }
}
