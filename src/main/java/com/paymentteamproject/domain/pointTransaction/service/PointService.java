package com.paymentteamproject.domain.pointTransaction.service;

import com.paymentteamproject.domain.membershipTransaction.exception.MembershipNotFoundException;
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


    // 주문 생성 시: 포인트 트랜잭션만 생성 (잔액 업데이트 X)
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
                    .type(PointTransactionType.PENDING)
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
            user.addPoints(pointTransaction.getPoints());
            userRepository.save(user);
        }
    }

    @Transactional
    public void usePoints(User user, Orders order,BigDecimal pointsToUse) {

        // 1. null / 0 이하 방어
        if (pointsToUse == null || pointsToUse.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");
        }

        // 2. 사용자 현재 포인트 조회
        BigDecimal currentPoints = user.getPointBalance();

        if (currentPoints == null) {
            currentPoints = BigDecimal.ZERO;
        }

        // 3. 잔액 부족 검증
        if (currentPoints.compareTo(pointsToUse) < 0) {
            throw new IllegalStateException("보유 포인트가 부족합니다.");
        }

        // 4. 포인트 차감
        user.subPoints(pointsToUse);

        // 5. 차감 트랜잭션 생성
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .order(order)
                .points(pointsToUse.negate()) //차감은 음수로 저장
                .type(PointTransactionType.USED)
                .build();

        pointTransactionRepository.save(transaction);

        // 6. 사용자 저장
        userRepository.save(user);
    }

    @Transactional
    public void refundPoints(User user, Orders order, BigDecimal pointsToRefund) {
        if (pointsToRefund == null || pointsToRefund.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("환불 포인트는 0보다 커야 합니다.");
        }

        // 2. 사용자 포인트 증가
        user.addPoints(pointsToRefund);

        // 사용자가 사용한 포인트 환불 이력
        PointTransaction recoveredTransaction = PointTransaction.builder()
                .user(user)
                .order(order)
                .points(pointsToRefund)
                .type(PointTransactionType.RECOVERED)
                .build();

        pointTransactionRepository.save(recoveredTransaction);

        // 3. 주문 적립 포인트 회수
        pointTransactionRepository
                .findByOrderAndTypeAndValidityTrue(order, PointTransactionType.ADDED)
                .ifPresent(earnedTransaction -> {
                    BigDecimal earnedPoint = earnedTransaction.getPoints();

                    //만료된 포인트일 경우 포인트 회수 미이행
                    if (!earnedTransaction.isValidity() || earnedTransaction.isExpired()) {
                        return;
                    }

                    // 유저 포인트 차감
                    // 현재 잔액 확인
                    BigDecimal currentBalance = user.getPointBalance();
                    BigDecimal recoverablePoint = earnedPoint.min(currentBalance);

                    if (recoverablePoint.compareTo(BigDecimal.ZERO) > 0) {
                        user.subPoints(recoverablePoint);
                    }

                    // 기존 적립 트랜잭션 무효화
                    earnedTransaction.isValid();

                    // 회수 이력 생성 (음수로 기록)
                    PointTransaction revokeTransaction = PointTransaction.builder()
                            .user(user)
                            .order(order)
                            .points(earnedPoint.negate())
                            .type(PointTransactionType.CANCELLED)
                            .build();

                    pointTransactionRepository.save(revokeTransaction);
                });

        userRepository.save(user);
    }

    @Transactional
    public void refundPointsForNoPointPayment(User user, Orders order) {
        // 주문 적립 포인트 회수
        pointTransactionRepository
                .findByOrderAndTypeAndValidityTrue(order, PointTransactionType.ADDED)
                .ifPresent(earnedTransaction -> {
                    BigDecimal earnedPoint = earnedTransaction.getPoints();

                    if (!earnedTransaction.isValidity() || earnedTransaction.isExpired()) {
                        return;
                    }

                    // 현재 사용자 포인트 확인
                    BigDecimal currentBalance = user.getPointBalance();
                    if (currentBalance == null) {
                        currentBalance = BigDecimal.ZERO;
                    }

                    // 회수 가능한 포인트 계산 (잔액과 적립 포인트 중 작은 값)
                    BigDecimal recoverablePoint = earnedPoint.min(currentBalance);

                    // 회수 가능한 포인트만큼만 차감
                    if (recoverablePoint.compareTo(BigDecimal.ZERO) > 0) {
                        user.subPoints(recoverablePoint);
                    }


                    // 유저 포인트 차감
                    user.subPoints(earnedPoint);

                    // 기존 적립 트랜잭션 무효화
                    earnedTransaction.isValid();

                    // 회수 이력 생성 (음수로 기록)
                    PointTransaction revokeTransaction = PointTransaction.builder()
                            .user(user)
                            .order(order)
                            .points(recoverablePoint.negate())
                            .type(PointTransactionType.CANCELLED)
                            .build();

                    pointTransactionRepository.save(revokeTransaction);
                });

        userRepository.save(user);
    }

}