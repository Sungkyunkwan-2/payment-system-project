package com.paymentteamproject.domain.pointTransaction.scheduler;

import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransactionType;
import com.paymentteamproject.domain.pointTransaction.repository.PointTransactionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PointScheduler {
    private final PointTransactionRepository pointTransactionRepository;
    private final UserRepository userRepository;

    // 매시간 정각에 실행
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processScheduledPoints() {
        LocalDateTime now = LocalDateTime.now();

        List<PointTransaction> pendingTransactions = pointTransactionRepository
                .findByTypeAndValidityTrueAndExpiresAtBefore(
                        PointTransactionType.PENDING,
                        now
                );

        for (PointTransaction pending : pendingTransactions) {
            Orders order = pending.getOrder();
            //환불 여부 확인
            if (!order.isRefunded()) {
                // 포인트 적립 처리
                PointTransaction addedTransaction = PointTransaction.builder()
                        .user(pending.getUser())
                        .order(pending.getOrder())
                        .points(pending.getPoints())
                        .type(PointTransactionType.ADDED)
                        //.expiresAt(LocalDateTime.now().plusMinutes(3))
                        .build();

                pointTransactionRepository.save(addedTransaction);

                User user = pending.getUser();
                user.addPoints(pending.getPoints());
                userRepository.save(user);

                pending.invalidate();
                pointTransactionRepository.save(pending);
            } else {
                // ORDER_CANCELED 상태면 적립 취소
                pending.invalidate();
                pointTransactionRepository.save(pending);
            }
        }
    }
}
