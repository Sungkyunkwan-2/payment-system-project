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

    /**
     * 주문 생성 시: 포인트 트랜잭션만 생성 (잔액 업데이트 X)
     */
    @Transactional
    public PointTransaction createEarnPointsTransaction(User user, Orders order) {
        // 1. 사용자의 현재 활성 멤버십 조회
        MembershipHistory activeMembership = membershipHistoryRepository
                .findByUserId(user.getId())
                .orElse(null);

        // 2. 적립 비율 결정 (멤버십 없으면 예외, 있으면 멤버십 비율)
        if (activeMembership == null) {
            throw new MembershipNotFoundException("멤버십이 존재하지 않습니다.");
        }
        BigDecimal ratio = activeMembership.getMembershipStatus().getRatio();

        // 3. 적립 포인트 계산 (주문 금액 * 적립 비율)
        BigDecimal earnedPoints = order.getTotalPrice().multiply(ratio);

        // 4. 포인트가 0보다 클 때만 트랜잭션 생성
        if (earnedPoints.signum() > 0) {
            PointTransaction pointTransaction = PointTransaction.builder()
                    .user(user)
                    .order(order)
                    .points(earnedPoints)
                    .type(PointTransactionType.ADDED)
                    .build();

            return pointTransactionRepository.save(pointTransaction);
        }

        return null;
    }


    //결제 완료 시: 사용자 포인트 잔액 업데이트
    @Transactional
    public void applyEarnedPoints(User user, Orders order) {
        // 해당 주문의 ADDED 타입 포인트 트랜잭션 조회
        PointTransaction pointTransaction = pointTransactionRepository
                .findByOrderAndType(order, PointTransactionType.ADDED)
                .orElse(null);

        if (pointTransaction != null && pointTransaction.getPoints().signum() > 0) {
            // 사용자 포인트 잔액 업데이트
            user.addPoints(pointTransaction.getPoints());
            userRepository.save(user);
        }
    }
}