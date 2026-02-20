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


    @Transactional
    public RefreshToken createRefreshToken(String email) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new TokenException("사용자를 찾을 수 없습니다."));

        String tokenValue = jwtTokenProvider.createRefreshToken(user.getEmail());
        Instant expiryDate = jwtTokenProvider.getRefreshTokenExpiryDate();

        return refreshTokenRepository.findByUser(user).map(existingToken -> {
            existingToken.updateToken(tokenValue, expiryDate);
            return existingToken;
        }).orElseGet(() -> refreshTokenRepository.save(new RefreshToken(tokenValue, user, expiryDate)));
    }

    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new TokenException("존재하지 않는 Refresh Token입니다."));

        if (refreshToken.isExpired()) {
            // 만료된 토큰은 DB에서 삭제
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException("만료된 토큰입니다. 다시 로그인해주세요.");
        }

        return refreshToken;
    }

    @Transactional
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
        log.info("Refresh Token 삭제 완료");
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
        log.info("만료된 Refresh Token 삭제 완료");
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenValue) {
        RefreshToken oldToken = verifyRefreshToken(oldTokenValue);
        String email = oldToken.getUser().getEmail();

        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(email);
    }
}
