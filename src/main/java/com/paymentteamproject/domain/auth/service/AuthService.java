package com.paymentteamproject.domain.auth.service;

import com.paymentteamproject.domain.auth.dto.LoginRequest;
import com.paymentteamproject.domain.auth.dto.TokenDto;
import com.paymentteamproject.domain.auth.entity.RefreshToken;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.security.CustomUserDetails;
import com.paymentteamproject.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenDto login(LoginRequest request) throws AuthenticationException {
        log.info("로그인 처리 시작: {}", request.getEmail());

        try {
            Authentication auth = AuthServiceSupport.validateRequest(authenticationManager, request.getEmail(), request.getPassword());
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            String accessToken = jwtTokenProvider.createToken(userDetails.getUsername(), userDetails.getName(), userDetails.getRoleAuthority());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());
            log.info("로그인 성공: {}", userDetails.getUsername());
            return new TokenDto(accessToken, refreshToken.getToken());
        } catch (AuthenticationException e) {
            log.warn("로그인 실패: {} - 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        log.info("로그아웃 처리 시작 - 토큰 {}", refreshToken);
        if (refreshToken != null) {
            refreshTokenService.deleteRefreshToken(refreshToken);
        }
        log.info("로그아웃 완료");
    }

    @Transactional
    public TokenDto refresh(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenService.verifyRefreshToken(refreshToken);
        User user = tokenEntity.getUser();
        String newAccessToken = jwtTokenProvider.createToken(user.getEmail(), user.getUsername(), user.getRole().getAuthority());
        RefreshToken rotatedToken = refreshTokenService.rotateRefreshToken(refreshToken);
        return new TokenDto(newAccessToken, rotatedToken.getToken());
    }
}
