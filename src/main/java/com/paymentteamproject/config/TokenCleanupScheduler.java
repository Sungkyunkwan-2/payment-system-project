package com.paymentteamproject.config;

import com.paymentteamproject.domain.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        log.info("만료된 Refresh Token 정리 작업 시작");
        refreshTokenService.deleteExpiredTokens();
        log.info("만료된 Refresh Token 정리 작업 완료");
    }

}

