package com.paymentteamproject.domain.refund.service;

import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.service.OrderService;
import com.paymentteamproject.domain.payment.consts.PaymentStatus;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.event.TotalSpendChangedEvent;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.pointTransaction.service.PointService;
import com.paymentteamproject.domain.refund.consts.RefundStatus;
import com.paymentteamproject.domain.refund.dto.PortOneCancelRequest;
import com.paymentteamproject.domain.refund.dto.RefundCreateRequest;
import com.paymentteamproject.domain.refund.dto.RefundCreateResponse;
import com.paymentteamproject.domain.refund.entity.Refund;
import com.paymentteamproject.domain.refund.exception.RefundInvalidStateException;
import com.paymentteamproject.domain.refund.repository.RefundRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock RefundRepository refundRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock UserRepository userRepository;
    @Mock RestClient portOneRestClient;
    @Mock OrderService orderService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock PointService pointService;

    @InjectMocks RefundService refundService;

    @Test
    void 결제상태가_SUCCESS가_아니면_환불불가() {
        // given
        String paymentId = "PAY_123";
        String email = "test@test.com";
        RefundCreateRequest request = new RefundCreateRequest();

        // userRepository mock
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // order mock (⭐ 추가)
        Orders order = mock(Orders.class);
        when(order.getUser()).thenReturn(user);

        // payment mock
        Payment payment = mock(Payment.class);
        when(payment.getOrder()).thenReturn(order);                 // (NPE 방지)
        when(payment.getStatus()).thenReturn(PaymentStatus.FAILURE);// SUCCESS 아님

        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId))
                .thenReturn(Optional.of(payment));

        // when & then
        assertThrows(RefundInvalidStateException.class,
                () -> refundService.requestRefund(paymentId, email, request));
    }

    @Test
    void PortOne취소_성공이면_SUCCESS_이벤트저장_및_후속처리() {
        // given
        String requestPaymentId = "pay_123";
        String email = "a@a.com";
        String portOnePaymentId = "portone_999";
        BigDecimal price = new BigDecimal("10000");

        RefundCreateRequest req = new RefundCreateRequest();
        ReflectionTestUtils.setField(req, "reason", "단순 변심");

        // User
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Order
        Orders order = mock(Orders.class);
        when(order.getUser()).thenReturn(user);
        when(order.getUsedPoint()).thenReturn(null);

        // Payment
        Payment payment = mock(Payment.class);
        when(payment.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(payment.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(2));
        when(payment.getPrice()).thenReturn(price);
        when(payment.getPaymentId()).thenReturn(portOnePaymentId);
        when(payment.getOrder()).thenReturn(order);

        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(requestPaymentId))
                .thenReturn(Optional.of(payment));

        // 멱등성: 최신 refund 없음
        when(refundRepository.findTopByPayment_PaymentIdOrderByIdDesc(requestPaymentId))
                .thenReturn(Optional.empty());

        // payment.refund() → refundedPayment
        // 여기서 “필드 스텁”은 최소화 (안 쓰이면 strict에서 터짐)
        Payment refundedPayment = mock(Payment.class);
        when(payment.refund()).thenReturn(refundedPayment);
        when(paymentRepository.save(refundedPayment)).thenReturn(refundedPayment);
        when(refundedPayment.getOrder()).thenReturn(order);

        // RestClient 체이닝 mock
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(portOneRestClient.post()).thenReturn(postSpec);
        when(postSpec.uri(eq("/payments/{paymentId}/cancel"), eq(portOnePaymentId)))
                .thenReturn(bodySpec);
        when(bodySpec.body(any(PortOneCancelRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        // when
        RefundCreateResponse res = refundService.requestRefund(requestPaymentId, email, req);

        // then: Refund 이벤트 2번 저장 (REQUEST + SUCCESS)
        ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository, times(2)).save(refundCaptor.capture());

        assertEquals(RefundStatus.REQUEST, refundCaptor.getAllValues().get(0).getStatus());
        assertEquals(RefundStatus.SUCCESS, refundCaptor.getAllValues().get(1).getStatus());

        // 후속 처리 verify (변수로 고정)
        verify(orderService, times(1)).processOrderCancellation(order);
        verify(pointService, times(1)).refundPointsForNoPointPayment(user, order);
        verify(pointService, never()).refundPoints(any(), any(), any());

        // 이벤트 publish 검증
        verify(eventPublisher, times(1)).publishEvent(any(TotalSpendChangedEvent.class));

        // 응답 SUCCESS
        assertEquals(RefundStatus.SUCCESS, res.getStatus());
    }

    @Test
    void PortOne취소_실패이면_FAILURE_이벤트저장() {
        // given
        String requestPaymentId = "pay_123";
        String email = "a@a.com";
        String portOnePaymentId = "portone_999";
        BigDecimal price = new BigDecimal("10000");

        RefundCreateRequest req = new RefundCreateRequest();
        ReflectionTestUtils.setField(req, "reason", "단순 변심");

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Payment payment = mock(Payment.class, RETURNS_DEEP_STUBS);
        when(payment.getId()).thenReturn(99L);
        when(payment.getStatus()).thenReturn(PaymentStatus.SUCCESS);
        when(payment.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(2));
        when(payment.getPrice()).thenReturn(price);
        when(payment.getPaymentId()).thenReturn(portOnePaymentId);
        when(payment.getOrder().getUser().getId()).thenReturn(1L);

        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(requestPaymentId))
                .thenReturn(Optional.of(payment));

        when(refundRepository.findTopByPayment_PaymentIdOrderByIdDesc(requestPaymentId))
                .thenReturn(Optional.empty());

        // RestClient 체이닝 mock -> toBodilessEntity에서 예외 발생시키기
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(portOneRestClient.post()).thenReturn(postSpec);
        when(postSpec.uri(eq("/payments/{paymentId}/cancel"), eq(portOnePaymentId))).thenReturn(bodySpec);
        when(bodySpec.body(any(PortOneCancelRequest.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);

        RestClientResponseException ex = new RestClientResponseException(
                "PortOne error", 400, "Bad Request",
                HttpHeaders.EMPTY, "fail".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8
        );
        when(responseSpec.toBodilessEntity()).thenThrow(ex);

        // when
        RefundCreateResponse res = refundService.requestRefund(requestPaymentId, email, req);

        // then: REQUEST 이벤트 저장 + FAILURE 이벤트 저장
        ArgumentCaptor<Refund> refundCaptor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository, times(2)).save(refundCaptor.capture());

        assertEquals(RefundStatus.REQUEST, refundCaptor.getAllValues().get(0).getStatus());
        assertEquals(RefundStatus.FAILURE, refundCaptor.getAllValues().get(1).getStatus());

        // 실패면 후속처리(주문취소/포인트/이벤트발행) 안 타야 정상
        verify(orderService, never()).processOrderCancellation(any());
        verify(pointService, never()).refundPoints(any(), any(), any());
        verify(pointService, never()).refundPointsForNoPointPayment(any(), any());
        verify(eventPublisher, never()).publishEvent(any());

        assertEquals(RefundStatus.FAILURE, res.getStatus());
    }
}