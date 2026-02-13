package com.paymentteamproject.domain.pointTransaction.service;

import com.paymentteamproject.domain.membershipTransaction.consts.MembershipStatus;
import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import com.paymentteamproject.domain.membershipTransaction.exception.MembershipNotFoundException;
import com.paymentteamproject.domain.membershipTransaction.repository.MembershipHistoryRepository;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransactionType;
import com.paymentteamproject.domain.pointTransaction.repository.PointTransactionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private MembershipHistoryRepository membershipHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @Mock
    private Orders order;

    @Mock
    private MembershipHistory membershipHistory;

    @Mock
    private MembershipStatus membershipStatus;

    @Test
    void 포인트적립트랜잭션_생성_성공() {

        BigDecimal totalPrice = BigDecimal.valueOf(10000);
        BigDecimal ratio = BigDecimal.valueOf(0.1);
        BigDecimal expectedPoints = BigDecimal.valueOf(1000.0);

        when(user.getId()).thenReturn(1L);
        when(order.getTotalPrice()).thenReturn(totalPrice);

        when(membershipHistoryRepository.findByUserId(1L))
                .thenReturn(Optional.of(membershipHistory));
        when(membershipHistory.getMembershipStatus())
                .thenReturn(membershipStatus);
        when(membershipStatus.getRatio())
                .thenReturn(ratio);

        when(pointTransactionRepository.save(any(PointTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PointTransaction result =
                pointService.createEarnPointsTransaction(user, order);

        assertNotNull(result);
        assertEquals(expectedPoints, result.getPoints());
        assertEquals(PointTransactionType.PENDING, result.getType());

        verify(pointTransactionRepository, times(1))
                .save(any(PointTransaction.class));
    }

    @Test
    void 멤버십없으면_예외발생() {

        when(user.getId()).thenReturn(1L);
        when(membershipHistoryRepository.findByUserId(1L))
                .thenReturn(Optional.empty());

        assertThrows(MembershipNotFoundException.class,
                () -> pointService.createEarnPointsTransaction(user, order));
    }

    @Test
    void 결제완료시_포인트적용_성공() {

        BigDecimal earnedPoints = BigDecimal.valueOf(500);

        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .order(order)
                .points(earnedPoints)
                .type(PointTransactionType.ADDED)
                .build();

        when(pointTransactionRepository.findByOrderAndType(order, PointTransactionType.ADDED))
                .thenReturn(Optional.of(transaction));

        pointService.applyEarnedPoints(user, order);

        verify(user, times(1)).addPoints(earnedPoints);
        verify(userRepository, times(1)).save(user);
    }


    @Test
    void 포인트사용_성공() {

        BigDecimal currentPoints = BigDecimal.valueOf(1000);
        BigDecimal usePoints = BigDecimal.valueOf(300);

        when(user.getPointBalance()).thenReturn(currentPoints);

        pointService.usePoints(user, order, usePoints);

        verify(user, times(1)).subPoints(usePoints);
        verify(pointTransactionRepository, times(1))
                .save(any(PointTransaction.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void 포인트부족시_예외발생() {

        when(user.getPointBalance())
                .thenReturn(BigDecimal.valueOf(100));

        assertThrows(IllegalStateException.class,
                () -> pointService.usePoints(user, order, BigDecimal.valueOf(500)));
    }

    @Test
    void 사용포인트가_0이하면_예외발생() {

        assertThrows(IllegalArgumentException.class,
                () -> pointService.usePoints(user, order, BigDecimal.ZERO));
    }

    @Test
    void 포인트환불_성공() {

        BigDecimal refundPoints = BigDecimal.valueOf(300);
        BigDecimal earnedPoints = BigDecimal.valueOf(500);

        PointTransaction earnedTransaction = PointTransaction.builder()
                .user(user)
                .order(order)
                .points(earnedPoints)
                .type(PointTransactionType.ADDED)
                .build();

        when(user.getPointBalance()).thenReturn(BigDecimal.valueOf(1000));

        when(pointTransactionRepository
                .findByOrderAndTypeAndValidityTrue(order, PointTransactionType.ADDED))
                .thenReturn(Optional.of(earnedTransaction));

        pointService.refundPoints(user, order, refundPoints);

        verify(user, times(1)).addPoints(refundPoints);
        verify(pointTransactionRepository, atLeastOnce())
                .save(any(PointTransaction.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void 포인트사용없는주문_환불시_적립포인트회수() {

        BigDecimal earnedPoints = BigDecimal.valueOf(500);

        PointTransaction earnedTransaction = PointTransaction.builder()
                .user(user)
                .order(order)
                .points(earnedPoints)
                .type(PointTransactionType.ADDED)
                .build();

        when(user.getPointBalance())
                .thenReturn(BigDecimal.valueOf(500));

        when(pointTransactionRepository
                .findByOrderAndTypeAndValidityTrue(order, PointTransactionType.ADDED))
                .thenReturn(Optional.of(earnedTransaction));

        pointService.refundPointsForNoPointPayment(user, order);

        verify(user, atLeastOnce()).subPoints(any());
        verify(pointTransactionRepository, times(1))
                .save(any(PointTransaction.class));
        verify(userRepository, times(1)).save(user);
    }
}
