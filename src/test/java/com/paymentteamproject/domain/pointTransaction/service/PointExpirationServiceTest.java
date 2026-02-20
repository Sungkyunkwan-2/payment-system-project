package com.paymentteamproject.domain.pointTransaction.service;

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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointExpirationServiceTest {

    @InjectMocks
    private PointExpirationService pointExpirationService;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private UserRepository userRepository;

    // 테스트용 PointTransaction 생성 헬퍼
    private PointTransaction createPointTransaction(User user, Orders order, BigDecimal points, PointTransactionType type) {
        return PointTransaction.builder()
                .user(user)
                .order(order)
                .points(points)
                .type(type)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 이미 만료된 시간
                .build();
    }

    @Test
    void 만료대상_포인트가_없으면_저장_및_잔액차감_호출_안됨() {
        // given
        given(pointTransactionRepository.findExpiredPoints(any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());

        // when
        pointExpirationService.expirePoints();

        // then
        verify(pointTransactionRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void 만료대상_포인트가_있으면_기존포인트_무효화_및_만료이력_저장_및_잔액차감() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        given(mockOrder.getId()).willReturn(1L);
        given(mockUser.getEmail()).willReturn("test@test.com");

        BigDecimal points = BigDecimal.valueOf(1000);
        PointTransaction originalPoint = createPointTransaction(mockUser, mockOrder, points, PointTransactionType.ADDED);

        given(pointTransactionRepository.findExpiredPoints(any(LocalDateTime.class)))
                .willReturn(List.of(originalPoint));

        // when
        pointExpirationService.expirePoints();

        // then
        // 기존 포인트 무효화 확인 (validity = false)
        assert !originalPoint.isValidity();

        // 만료 이력이 EXPIRED 타입으로 저장되었는지 확인
        verify(pointTransactionRepository, times(1)).save(argThat(saved ->
                saved.getType() == PointTransactionType.EXPIRED
                        && saved.getPoints().equals(points)
                        && saved.getUser().equals(mockUser)
                        && saved.getOrder().equals(mockOrder)
        ));

        // 사용자 잔액 차감 확인
        verify(mockUser, times(1)).expirePoints(points);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void 만료대상_포인트가_여러건이면_건수만큼_반복처리() {
        // given
        User mockUser1 = mock(User.class);
        User mockUser2 = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        given(mockOrder.getId()).willReturn(1L);
        given(mockUser1.getEmail()).willReturn("user1@test.com");
        given(mockUser2.getEmail()).willReturn("user2@test.com");

        BigDecimal points1 = BigDecimal.valueOf(500);
        BigDecimal points2 = BigDecimal.valueOf(300);

        PointTransaction originalPoint1 = createPointTransaction(mockUser1, mockOrder, points1, PointTransactionType.ADDED);
        PointTransaction originalPoint2 = createPointTransaction(mockUser2, mockOrder, points2, PointTransactionType.ADDED);

        given(pointTransactionRepository.findExpiredPoints(any(LocalDateTime.class)))
                .willReturn(List.of(originalPoint1, originalPoint2));

        // when
        pointExpirationService.expirePoints();

        // then
        verify(pointTransactionRepository, times(2)).save(any(PointTransaction.class));
        verify(mockUser1, times(1)).expirePoints(points1);
        verify(mockUser2, times(1)).expirePoints(points2);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void PENDING_타입_포인트도_만료처리됨() {
        // given
        User mockUser = mock(User.class);
        Orders mockOrder = mock(Orders.class);
        given(mockOrder.getId()).willReturn(1L);
        given(mockUser.getEmail()).willReturn("test@test.com");

        BigDecimal points = BigDecimal.valueOf(200);
        PointTransaction pendingPoint = createPointTransaction(mockUser, mockOrder, points, PointTransactionType.PENDING);

        given(pointTransactionRepository.findExpiredPoints(any(LocalDateTime.class)))
                .willReturn(List.of(pendingPoint));

        // when
        pointExpirationService.expirePoints();

        // then
        verify(pointTransactionRepository, times(1)).save(argThat(saved ->
                saved.getType() == PointTransactionType.EXPIRED
                        && saved.getPoints().equals(points)
        ));
        verify(mockUser, times(1)).expirePoints(points);
        verify(userRepository, times(1)).save(mockUser);
    }
}