package com.paymentteamproject.domain.auth.service;

import com.paymentteamproject.domain.auth.dto.LoginRequest;
import com.paymentteamproject.domain.auth.dto.TokenDto;
import com.paymentteamproject.domain.auth.entity.RefreshToken;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import com.paymentteamproject.security.CustomUserDetails;
import com.paymentteamproject.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final UserRepository userRepository;

    @Transactional
    public TokenDto login(LoginRequest request) throws AuthenticationException {
        log.info("로그인 처리 시작: {}", request.getEmail());

        // 1. 이메일/비밀번호 검증
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            // 토큰 생성을 위해 인증된 사용자 정보 추출
            if (auth.getPrincipal() == null) {
                throw new BadCredentialsException("Authentication principal is null");
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

            log.info("로그인 성공: {}", userDetails.getUsername());

            return new TokenDto(accessToken, refreshToken.getToken());
        } catch (AuthenticationException e) {
            log.warn("로그인 실패: {} - 사유: {}", request.getEmail(), e.getMessage());
            throw e;
        }

    }

    @Transactional
    public TokenDto refresh(String refreshToken) {
        // 1. 리프레시 토큰 검증 (DB 조회 및 만료 확인)
        // 이전에 구현한 refreshTokenService.verifyRefreshToken을 활용합니다.
        RefreshToken tokenEntity = refreshTokenService.verifyRefreshToken(refreshToken);

        // 2. 유저 정보 추출
        User user = tokenEntity.getUser();

        // 3. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createToken(
                user.getEmail(),
                user.getUsername(),
                user.getRole().getAuthority() // 유저 엔티티의 권한 필드명에 맞춰 수정하세요
        );

        // 4. (선택) Refresh Token Rotation: 리프레시 토큰도 새로 발급하여 보안 강화
        RefreshToken rotatedToken = refreshTokenService.rotateRefreshToken(refreshToken);

        return new TokenDto(newAccessToken, rotatedToken.getToken());
    }
}
