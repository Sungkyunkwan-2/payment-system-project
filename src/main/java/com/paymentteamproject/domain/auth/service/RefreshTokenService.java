package com.paymentteamproject.domain.auth.service;

import com.paymentteamproject.domain.auth.entity.RefreshToken;
import com.paymentteamproject.domain.auth.exception.TokenException;
import com.paymentteamproject.domain.auth.repository.RefreshTokenRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import com.paymentteamproject.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * Refresh Token 생성 및 저장
     * - 기존 토큰이 있으면 삭제하고 새로 생성 (1 User = 1 Refresh Token)
     */

    @Transactional
    public RefreshToken createRefreshToken(String email) {

        // 1. 유저 조회
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new TokenException("사용자를 찾을 수 없습니다.")
        );

        // 2. 기존 토큰 삭제 (사용자당 하나의 토큰만 유지)
        refreshTokenRepository.deleteByUser(user);

        // 3. 새 Refresh Token 생성
        String tokenValue = jwtTokenProvider.createRefreshToken(user.getEmail());
        Instant expiryDate = jwtTokenProvider.getRefreshTokenExpiryDate();

        // 5. 엔티티 생성 및 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Refresh Token 검증
     * - DB에서 토큰 조회 및 만료 여부 확인
     */
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("유효하지 않은 Refresh Token입니다."));

        if (refreshToken.isExpired()) {
            // 만료된 토큰은 DB에서 삭제
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
        }

        return refreshToken;
    }

    /**
     * Refresh Token 삭제 (로그아웃 시 사용)
     */
    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
        log.info("Refresh Token 삭제 완료");
    }

    /**
     * 사용자의 모든 Refresh Token 삭제
     */
    @Transactional
    public void deleteAllUserTokens(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new TokenException("사용자를 찾을 수 없습니다."));
        refreshTokenRepository.deleteByUser(user);
        log.info("사용자의 모든 Refresh Token 삭제: {}", email);
    }

    /**
     * 만료된 토큰 일괄 삭제 (스케줄러에서 호출)
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("만료된 Refresh Token 삭제 완료");
    }

    /**
     * Refresh Token Rotation (선택적 보안 강화)
     * - 토큰 재발급 시 새 Refresh Token도 함께 발급
     */
    /**
     * Refresh Token Rotation (보안 강화)
     * - 액세스 토큰 재발급 시 리프레시 토큰도 새 값으로 교체
     */
    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenValue) {
        RefreshToken oldToken = verifyRefreshToken(oldTokenValue);
        String email = oldToken.getUser().getEmail();

        // 기존 토큰 삭제 후 새 토큰 생성
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(email);
    }
}
