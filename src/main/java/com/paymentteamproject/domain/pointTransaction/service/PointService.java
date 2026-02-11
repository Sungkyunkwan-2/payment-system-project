package com.paymentteamproject.domain.pointTransaction.service;

import com.paymentteamproject.domain.masterMembership.exception.MembershipNotFoundException;
import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import com.paymentteamproject.domain.membershipTransaction.repository.MembershipHistoryRepository;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransactionType;
import com.paymentteamproject.domain.pointTransaction.repository.PointTransactionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class PointService {
    private final PointTransactionRepository pointTransactionRepository;
    private final MembershipHistoryRepository membershipHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public PointTransaction earnPoints(User user, Orders order) {
        // 1. 사용자의 현재 활성 멤버십 조회
        MembershipHistory activeMembership = membershipHistoryRepository
                .findByUserId(user.getId())
                .orElse(null);

        // 2. 적립 비율 결정 (멤버십 없으면 예외, 있으면 멤버십 비율)

        if (activeMembership == null) {
            throw  new MembershipNotFoundException("멤버십이 존재하지 않습니다.");
        }
        BigDecimal ratio = activeMembership.getMembershipStatus().getRatio();

        // 3. 적립 포인트 계산 (주문 금액 * 적립 비율)
        BigDecimal earnedPoints = order.getTotalPrice().multiply(ratio);

        // 4. 포인트가 0보다 클 때만 적립
        if (earnedPoints.signum() > 0) {
            PointTransaction pointTransaction = PointTransaction.builder()
                    .user(user)
                    .order(order)
                    .points(earnedPoints)
                    .type(PointTransactionType.ADDED)
                    .build();

            PointTransaction savedTransaction = pointTransactionRepository.save(pointTransaction);

            // 5. 사용자 포인트 잔액 업데이트
            user.addPoints(earnedPoints);
            userRepository.save(user);
            //TODO 이걸 결제로 옮겨야 함

            return savedTransaction;
        }

        return null;
    }
}