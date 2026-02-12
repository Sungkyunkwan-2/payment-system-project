package com.paymentteamproject.config;

import com.paymentteamproject.domain.user.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 만료된 Refresh Token 자동 정리 스케줄러
 * - 매일 새벽 3시에 실행
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    /**
     * 만료된 토큰 정리
     * - cron: 매일 새벽 3시 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        log.info("만료된 Refresh Token 정리 작업 시작");
        refreshTokenService.deleteExpiredTokens();
        log.info("만료된 Refresh Token 정리 작업 완료");
    }

}

