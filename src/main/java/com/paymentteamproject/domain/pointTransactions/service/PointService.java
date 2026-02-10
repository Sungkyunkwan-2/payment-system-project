package com.paymentteamproject.domain.pointTransactions.service;

import com.paymentteamproject.domain.masterMembership.exception.MembershipNotFoundException;
import com.paymentteamproject.domain.membershipTransaction.entity.MembershipTransaction;
import com.paymentteamproject.domain.membershipTransaction.repository.MembershipTransactionRepository;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.pointTransactions.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransactions.entity.PointTransactionType;
import com.paymentteamproject.domain.pointTransactions.repository.PointTransactionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


//@Service
//@RequiredArgsConstructor
//public class PointService {
//    private final PointTransactionRepository pointTransactionRepository;
//    private final MembershipTransactionRepository membershipTransactionRepository;
//    private final UserRepository userRepository;
//
//    @Transactional
//    public PointTransaction earnPoints(User user, Orders order) {
//        // 1. 사용자의 현재 활성 멤버십 조회
//        MembershipTransaction activeMembership = membershipTransactionRepository
//                .findByUserId(user.getId())
//                .orElse(null);
//
//        // 2. 적립 비율 결정 (멤버십 없으면 예외, 있으면 멤버십 비율)
//        double ratio = 0.0;
//        if (activeMembership == null) {
//            throw  new MembershipNotFoundException("멤버십이 존재하지 않습니다.");
//        }
//        ratio = activeMembership.getMasterMembership().getRatio();
//
//        // 3. 적립 포인트 계산 (주문 금액 * 적립 비율)
//        double earnedPoints = order.getTotalPrice() * ratio;
//
//        // 4. 포인트가 0보다 클 때만 적립
//        if (earnedPoints > 0) {
//            PointTransaction pointTransaction = PointTransaction.builder()
//                    .user(user)
//                    .order(order)
//                    .points(earnedPoints)
//                    .type(PointTransactionType.ADDED)
//                    .build();
//
//            PointTransaction savedTransaction = pointTransactionRepository.save(pointTransaction);
//
//            // 5. 사용자 포인트 잔액 업데이트
//            user.addPoints(earnedPoints);
//            userRepository.save(user);
//
//            return savedTransaction;
//        }
//
//        return null;
//    }
//}