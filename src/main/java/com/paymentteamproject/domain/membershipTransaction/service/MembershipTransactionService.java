package com.paymentteamproject.domain.membershipTransaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipTransactionService {

    @Scheduled(cron = "0 0 0 * * *") // 매일 새벽 2시 실행
    public void updateMembership() {
        // 1. 등급 재계산 대상자 조회
        // 2. 등급 로직 실행
        // 3. DB 업데이트
    }
}
