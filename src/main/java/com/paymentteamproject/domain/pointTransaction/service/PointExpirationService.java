package com.paymentteamproject.domain.pointTransaction.service;

import com.paymentteamproject.domain.pointTransaction.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransaction.repository.PointTransactionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointExpirationService {
    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    //만료된 포인트 일괄 처리
    //매일 자정에 실행 (cron = "0 0 0 * * *")
    //TODO: 테스트용: 1분마다 실행 (fixedRate = 60000) 추후 운영 단계에서 아래 매일 자정 실행으로 수정
    //@Scheduled(fixedRate = 60000) // 1분마다 실행 (테스트용)
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행 (운영용)
    @Transactional
    public void expirePoints() {
        log.info("포인트 만료 처리 시작");

        LocalDateTime now = LocalDateTime.now();
        List<PointTransaction> expiredPoints = pointTransactionRepository.findExpiredPoints(now);

        if (expiredPoints.isEmpty()) {
            log.info("만료 대상 포인트 없음");
            return;
        }

        log.info("만료 대상 포인트 {}건 발견", expiredPoints.size());

        for (PointTransaction originalPoint : expiredPoints) {
            // 1. 기존 적립 포인트 무효화
            originalPoint.invalidate();

            // 2. 만료 이력 생성 (새로운 레코드)
            PointTransaction expiredRecord = PointTransaction.createExpiredRecord(originalPoint);
            pointTransactionRepository.save(expiredRecord);

            // 3. 사용자 포인트 잔액 차감
            User user = originalPoint.getUser();
            user.expirePoints(originalPoint.getPoints());
            userRepository.save(user);

            log.info("포인트 만료 처리 완료 - 사용자: {}, 주문: {}, 포인트: {}, 원본ID: {}, 만료ID: {}",
                    user.getEmail(),
                    originalPoint.getOrder().getId(),
                    originalPoint.getPoints(),
                    originalPoint.getId(),
                    expiredRecord.getId());
        }

        log.info("포인트 만료 처리 완료: 총 {}건", expiredPoints.size());
    }
}