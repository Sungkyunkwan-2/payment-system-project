package com.paymentteamproject.domain.pointTransaction.service;

import com.paymentteamproject.domain.membershipTransaction.consts.MembershipStatus;
import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import com.paymentteamproject.domain.membershipTransaction.exception.MembershipNotFoundException;
import com.paymentteamproject.domain.membershipTransaction.repository.MembershipHistoryRepository;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransactionType;
import com.paymentteamproject.domain.pointTransaction.exception.InsufficientPointException;
import com.paymentteamproject.domain.pointTransaction.exception.InvalidPointAmountException;
import com.paymentteamproject.domain.pointTransaction.exception.InvalidRefundPointAmountException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
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

    // ==================== createEarnPointsTransaction ====================

    @Test
    void 주문생성시_멤버십이_없으면_MembershipNotFoundException_발생() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        given(mockUser.getId()).willReturn(1L);
        given(membershipHistoryRepository.findByUserId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.createEarnPointsTransaction(mockUser, mockOrder))
                .isInstanceOf(MembershipNotFoundException.class)
                .hasMessage("멤버십이 존재하지 않습니다.");

        verify(pointTransactionRepository, never()).save(any());
    }

    @Test
    void 주문생성시_적립포인트가_0보다_크면_PENDING_트랜잭션_생성() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        MembershipHistory mockMembership = mock(MembershipHistory.class);

        given(mockUser.getId()).willReturn(1L);
        given(mockOrder.getTotalPrice()).willReturn(BigDecimal.valueOf(10000));
        given(membershipHistoryRepository.findByUserId(1L)).willReturn(Optional.of(mockMembership));
        given(mockMembership.getMembershipStatus()).willReturn(MembershipStatus.BRONZE); // BRONZE: 0.1%

        PointTransaction savedTransaction = mock(PointTransaction.class);
        given(pointTransactionRepository.save(any(PointTransaction.class))).willReturn(savedTransaction);

        // when
        PointTransaction result = pointService.createEarnPointsTransaction(mockUser, mockOrder);

        // then
        // 10000 * 0.001 = 10
        BigDecimal expectedPoints = BigDecimal.valueOf(10000).multiply(MembershipStatus.BRONZE.getRatio());

        assertThat(result).isNotNull();
        verify(pointTransactionRepository, times(1)).save(argThat(tx ->
                tx.getType() == PointTransactionType.PENDING
                        && tx.getPoints().compareTo(expectedPoints) == 0
                        && tx.getUser().equals(mockUser)
                        && tx.getOrder().equals(mockOrder)
        ));
    }

    @Test
    void 주문생성시_적립포인트가_0보다_크면_PENDING_트랜잭션_생성_GOLD등급() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        MembershipHistory mockMembership = mock(MembershipHistory.class);

        given(mockUser.getId()).willReturn(1L);
        given(mockOrder.getTotalPrice()).willReturn(BigDecimal.valueOf(10000));
        given(membershipHistoryRepository.findByUserId(1L)).willReturn(Optional.of(mockMembership));
        given(mockMembership.getMembershipStatus()).willReturn(MembershipStatus.GOLD); // GOLD: 1%

        PointTransaction savedTransaction = mock(PointTransaction.class);
        given(pointTransactionRepository.save(any(PointTransaction.class))).willReturn(savedTransaction);

        // when
        PointTransaction result = pointService.createEarnPointsTransaction(mockUser, mockOrder);

        // then
        // 10000 * 0.01 = 100
        BigDecimal expectedPoints = BigDecimal.valueOf(10000).multiply(MembershipStatus.GOLD.getRatio());

        assertThat(result).isNotNull();
        verify(pointTransactionRepository, times(1)).save(argThat(tx ->
                tx.getType() == PointTransactionType.PENDING
                        && tx.getPoints().compareTo(expectedPoints) == 0
                        && tx.getUser().equals(mockUser)
                        && tx.getOrder().equals(mockOrder)
        ));
    }

    // ==================== applyEarnedPoints ====================

    @Test
    void 결제완료시_ADDED_타입_트랜잭션이_있으면_사용자_포인트_잔액_증가() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        PointTransaction mockTransaction = mock(PointTransaction.class);

        given(pointTransactionRepository.findByOrderAndType(mockOrder, PointTransactionType.ADDED))
                .willReturn(Optional.of(mockTransaction));
        given(mockTransaction.getPoints()).willReturn(BigDecimal.valueOf(500));

        // when
        pointService.applyEarnedPoints(mockUser, mockOrder);

        // then
        verify(mockUser, times(1)).addPoints(BigDecimal.valueOf(500));
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void 결제완료시_ADDED_타입_트랜잭션이_없으면_포인트_잔액_변경_안됨() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);

        given(pointTransactionRepository.findByOrderAndType(mockOrder, PointTransactionType.ADDED))
                .willReturn(Optional.empty());

        // when
        pointService.applyEarnedPoints(mockUser, mockOrder);

        // then
        verify(mockUser, never()).addPoints(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void 결제완료시_ADDED_타입_포인트가_0이하이면_포인트_잔액_변경_안됨() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        PointTransaction mockTransaction = mock(PointTransaction.class);

        given(pointTransactionRepository.findByOrderAndType(mockOrder, PointTransactionType.ADDED))
                .willReturn(Optional.of(mockTransaction));
        given(mockTransaction.getPoints()).willReturn(BigDecimal.ZERO);

        // when
        pointService.applyEarnedPoints(mockUser, mockOrder);

        // then
        verify(mockUser, never()).addPoints(any());
        verify(userRepository, never()).save(any());
    }

    // ==================== usePoints ====================

    @Test
    void 포인트사용시_사용포인트가_null이면_InvalidPointAmountException_발생() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);

        // when & then
        assertThatThrownBy(() -> pointService.usePoints(mockUser, mockOrder, null))
                .isInstanceOf(InvalidPointAmountException.class)
                .hasMessage("사용 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트사용시_사용포인트가_0이하이면_InvalidPointAmountException_발생() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);

        // when & then
        assertThatThrownBy(() -> pointService.usePoints(mockUser, mockOrder, BigDecimal.ZERO))
                .isInstanceOf(InvalidPointAmountException.class)
                .hasMessage("사용 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트사용시_잔액이_부족하면_InsufficientPointException_발생() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);

        given(mockUser.getPointBalance()).willReturn(BigDecimal.valueOf(100));

        // when & then
        assertThatThrownBy(() -> pointService.usePoints(mockUser, mockOrder, BigDecimal.valueOf(500)))
                .isInstanceOf(InsufficientPointException.class)
                .hasMessage("보유 포인트가 부족합니다.");
    }

    @Test
    void 포인트사용시_잔액이_충분하면_포인트_차감_및_USED_트랜잭션_생성() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        BigDecimal pointsToUse = BigDecimal.valueOf(300);

        given(mockUser.getPointBalance()).willReturn(BigDecimal.valueOf(1000));

        // when
        pointService.usePoints(mockUser, mockOrder, pointsToUse);

        // then
        verify(mockUser, times(1)).subPoints(pointsToUse);
        verify(pointTransactionRepository, times(1)).save(argThat(tx ->
                tx.getType() == PointTransactionType.USED
                        && tx.getPoints().compareTo(pointsToUse.negate()) == 0
                        && tx.getUser().equals(mockUser)
                        && tx.getOrder().equals(mockOrder)
        ));
        verify(userRepository, times(1)).save(mockUser);
    }

    // ==================== refundPoints ====================

    @Test
    void 포인트환불시_환불포인트가_null이면_InvalidRefundPointAmountException_발생() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);

        // when & then
        assertThatThrownBy(() -> pointService.refundPoints(mockUser, mockOrder, null))
                .isInstanceOf(InvalidRefundPointAmountException.class)
                .hasMessage("환불 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트환불시_환불포인트가_0이하이면_InvalidRefundPointAmountException_발생() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);

        // when & then
        assertThatThrownBy(() -> pointService.refundPoints(mockUser, mockOrder, BigDecimal.ZERO))
                .isInstanceOf(InvalidRefundPointAmountException.class)
                .hasMessage("환불 포인트는 0보다 커야 합니다.");
    }

    @Test
    void 포인트환불시_ADDED_트랜잭션이_없으면_사용포인트만_환불되고_적립포인트_회수_안됨() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        BigDecimal refundAmount = BigDecimal.valueOf(300);

        given(pointTransactionRepository.findByOrderAndTypeAndValidityTrue(mockOrder, PointTransactionType.ADDED))
                .willReturn(Optional.empty());

        // when
        pointService.refundPoints(mockUser, mockOrder, refundAmount);

        // then
        verify(mockUser, times(1)).addPoints(refundAmount);
        verify(pointTransactionRepository, times(1)).save(argThat(tx ->
                tx.getType() == PointTransactionType.RECOVERED
        ));
        verify(mockUser, never()).subPoints(any());
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void 포인트환불시_유효한_ADDED_트랜잭션이_있으면_사용포인트_환불_및_적립포인트_회수() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        BigDecimal refundAmount = BigDecimal.valueOf(300);
        BigDecimal earnedPoints = BigDecimal.valueOf(500);

        PointTransaction mockEarnedTransaction = mock(PointTransaction.class);
        given(mockEarnedTransaction.getPoints()).willReturn(earnedPoints);
        given(mockEarnedTransaction.isValidity()).willReturn(true);
        given(mockEarnedTransaction.isExpired()).willReturn(false);
        given(mockUser.getPointBalance()).willReturn(BigDecimal.valueOf(1000));

        given(pointTransactionRepository.findByOrderAndTypeAndValidityTrue(mockOrder, PointTransactionType.ADDED))
                .willReturn(Optional.of(mockEarnedTransaction));

        // when
        pointService.refundPoints(mockUser, mockOrder, refundAmount);

        // then
        verify(mockUser, times(1)).addPoints(refundAmount);
        verify(mockUser, times(1)).subPoints(earnedPoints); // 적립 포인트 회수
        verify(mockEarnedTransaction, times(1)).isValid();  // 기존 트랜잭션 무효화

        verify(pointTransactionRepository, times(2)).save(argThat(tx ->
                tx.getType() == PointTransactionType.RECOVERED
                        || tx.getType() == PointTransactionType.CANCELLED
        ));
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void 포인트환불시_만료된_ADDED_트랜잭션이면_적립포인트_회수_안됨() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        BigDecimal refundAmount = BigDecimal.valueOf(300);

        PointTransaction mockEarnedTransaction = mock(PointTransaction.class);
        given(mockEarnedTransaction.isValidity()).willReturn(true);
        given(mockEarnedTransaction.isExpired()).willReturn(true); // 만료된 트랜잭션

        given(pointTransactionRepository.findByOrderAndTypeAndValidityTrue(mockOrder, PointTransactionType.ADDED))
                .willReturn(Optional.of(mockEarnedTransaction));

        // when
        pointService.refundPoints(mockUser, mockOrder, refundAmount);

        // then
        verify(mockUser, times(1)).addPoints(refundAmount);
        verify(mockUser, never()).subPoints(any()); // 회수 안됨
        verify(mockEarnedTransaction, never()).isValid();
        verify(userRepository, times(1)).save(mockUser);
    }
}