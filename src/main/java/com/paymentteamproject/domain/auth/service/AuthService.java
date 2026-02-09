package com.paymentteamproject.domain.auth.service;

import com.paymentteamproject.domain.auth.dto.LoginRequest;
import com.paymentteamproject.domain.auth.dto.TokenDto;
import com.paymentteamproject.domain.auth.entity.RefreshToken;
import com.paymentteamproject.domain.user.repository.UserRepository;
import com.paymentteamproject.security.CustomUserDetails;
import com.paymentteamproject.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;


    public TokenDto login(LoginRequest request) throws AuthenticationException {
        log.info("로그인 처리 시작: {}", request.getEmail());

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

        // 2. 인증 성공 시 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createToken(
                userDetails.getUsername(), // email
                userDetails.getName(), // username
                userDetails.getRoleAuthority() // role
        );

        // 3. Refesh Token 생성 및 DB 저장 (이메일만 사용)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());


    }
}
