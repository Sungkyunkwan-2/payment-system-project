package com.paymentteamproject.domain.payment.service;

import com.paymentteamproject.domain.order.consts.OrderStatus;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.exception.OrderAccessException;
import com.paymentteamproject.domain.order.exception.OrderNotFoundException;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.payment.consts.PaymentStatus;
import com.paymentteamproject.domain.payment.dto.ConfirmPaymentResponse;
import com.paymentteamproject.domain.payment.dto.PortOnePaymentResponse;
import com.paymentteamproject.domain.payment.dto.StartPaymentRequest;
import com.paymentteamproject.domain.payment.dto.StartPaymentResponse;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.exception.DuplicatePaymentConfirmException;
import com.paymentteamproject.domain.payment.exception.PaymentNotFoundException;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.pointTransaction.service.PointService;
import com.paymentteamproject.domain.refund.service.RefundService;
import com.paymentteamproject.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RefundService refundService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PointService pointService;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private PaymentService paymentService;

    private User testUser;
    private Orders testOrder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .username("테스트유저")
                .phone("010-1234-5678")
                .email("test@example.com")
                .password("12345678")
                .pointBalance(BigDecimal.valueOf(5000.0))
                .build();
        // Reflection으로 ID 설정
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 테스트 주문 생성
        testOrder = Orders.builder()
                .user(testUser)
                .orderNumber(120260209150134L)
                .totalPrice(BigDecimal.valueOf(10000.0))
                .usedPoint(BigDecimal.valueOf(0.0))
                .status(OrderStatus.PAYMENT_PENDING)
                .build();
        // Reflection으로 ID 설정
        ReflectionTestUtils.setField(testOrder, "id", 1L);

        // UTC 기준
        Clock utcClock = Clock.systemUTC();

        // 테스트 결제 생성
        testPayment = Payment.start(testOrder, BigDecimal.valueOf(10000.0));
        ReflectionTestUtils.setField(testPayment, "id", 1L);
        ReflectionTestUtils.setField(testPayment, "createdAt", LocalDateTime.now(utcClock));

        lenient().when(restClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    // ========== 결제 시작 테스트 ==========

    @Test
    @DisplayName("결제 시작 성공")
    void startPayment_Success() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            ReflectionTestUtils.setField(payment, "id", 1L);
            ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.now());
            return payment;
        });

        // when
        StartPaymentResponse response = paymentService.start(
                createStartPaymentRequest(1L)
        );

        // then
        assertNotNull(response);
        assertThat(response.getPaymentId()).startsWith("PAY1");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertNotNull(response.getCreatedAt());

        verify(orderRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 시작 실패 - 존재하지 않는 주문")
    void startPayment_OrderNotFound() {
        // given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> paymentService.start(createStartPaymentRequest(999L))
        );

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 주문입니다");
        verify(orderRepository, times(1)).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 시작 실패 - 이미 결제 완료된 주문")
    void startPayment_AlreadyPaid() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        testOrder.updateStatus(OrderStatus.ORDER_COMPLETED);

        // when & then
        OrderAccessException exception = assertThrows(
                OrderAccessException.class,
                () -> paymentService.start(createStartPaymentRequest(1L))
        );

        assertThat(exception.getMessage()).contains("주문이 결제를 진행할 수 없는 상태입니다.");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    // ========== 결제 확인 테스트 ==========

    @Test
    @DisplayName("결제 확인 성공 - PAID 상태이고 금액 일치")
    void confirmPayment_Success() {
        // given
        String paymentId = testPayment.getPaymentId();

        // [수정] 메서드명 일치
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId))
                .thenReturn(Optional.of(testPayment));

        // PortOne API 응답 Mock (결제 금액 10000원 가정)
        mockPortOneApiCall("PAID", 10000);

        // save 시 전달받은 객체 그대로 반환
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // [중요] 포인트 서비스 및 이벤트 발행 시 오류가 나지 않도록 Mock 설정 확인 필요
        // 별도의 stubbing(when)이 없어도 @Mock으로 선언되어 있다면 void 메서드는 무시되지만,
        // 만약 필드 주입이 안 되어 null 상태라면 여기서 터집니다.

        // when
        ConfirmPaymentResponse response = paymentService.confirm(paymentId);

        // then
        assertNotNull(response);
        // [수정] assertThat 사용법 (isEqualTo 권장)
        assertThat(response.getOrderId()).isEqualTo(testOrder.getOrderNumber());
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        // [수정] 실제 호출하는 메서드로 검증
        verify(paymentRepository, times(1)).findFirstByPaymentIdOrderByIdDesc(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 확인 실패 - 존재하지 않는 결제")
    void confirmPayment_PaymentNotFound() {
        // given
        String paymentId = "INVALID_PAY_ID";
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId)).thenReturn(Optional.empty());

        // when & then
        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.confirm(paymentId)
        );

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 결제입니다.");
        verify(paymentRepository, times(1)).findFirstByPaymentIdOrderByIdDesc(paymentId);
        verify(restClient, never()).get();
    }

    @Test
    @DisplayName("결제 확인 실패 - 성공한 결제")
    void confirmPayment_AlreadySuccess() {
        // given
        String paymentId = testPayment.getPaymentId();
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId)).thenReturn(Optional.of(testPayment));
        testPayment.updateStatus(PaymentStatus.SUCCESS);

        // when & then
        DuplicatePaymentConfirmException exception = assertThrows(
                DuplicatePaymentConfirmException.class,
                () -> paymentService.confirm(paymentId)
        );

        assertThat(exception.getMessage()).isEqualTo("이미 처리 중이거나 완료된 결제입니다.");
        verify(paymentRepository, times(1)).findFirstByPaymentIdOrderByIdDesc(paymentId);
        verify(restClient, never()).get();

    }

    @Test
    @DisplayName("결제 확인 실패 - PortOne API 응답 null")
    void confirmPayment_PortOneResponseNull() {
        // given
        String paymentId = testPayment.getPaymentId();
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId)).thenReturn(Optional.of(testPayment));

        // PortOne API 응답 Mock - 상태가 NULL
        when(responseSpec.body(PortOnePaymentResponse.class)).thenReturn(null);

        // when & then
        PaymentNotFoundException exception = assertThrows(
                PaymentNotFoundException.class,
                () -> paymentService.confirm(paymentId)
        );

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 결제입니다.");
        verify(paymentRepository, times(1)).findFirstByPaymentIdOrderByIdDesc(paymentId);
    }

    @Test
    @DisplayName("결제 확인 실패 - 상태가 PAID가 아님")
    void confirmPayment_StatusNotPaid() {
        // given
        String paymentId = testPayment.getPaymentId();
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId)).thenReturn(Optional.of(testPayment));

        // PortOne API 응답 Mock - 상태가 READY
        mockPortOneApiCall("READY", 10000);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ConfirmPaymentResponse response = paymentService.confirm(paymentId);

        // then
        assertNotNull(response);
        assertThat(response.getOrderId()).isEqualTo(testOrder.getOrderNumber());
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILURE);

        verify(paymentRepository, times(1)).findFirstByPaymentIdOrderByIdDesc(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 확인 실패 - 금액 불일치")
    void confirmPayment_AmountMismatch() {
        // given
        String paymentId = testPayment.getPaymentId();
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId)).thenReturn(Optional.of(testPayment));

        // PortOne API 응답 Mock - 금액 불일치
        mockPortOneApiCall("PAID", 5000);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ConfirmPaymentResponse response = paymentService.confirm(paymentId);

        // then
        assertNotNull(response);
        assertThat(response.getOrderId()).isEqualTo(testOrder.getOrderNumber());
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILURE);

        verify(paymentRepository, times(1)).findFirstByPaymentIdOrderByIdDesc(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 확인 실패 - 상태가 PAID가 아니면서 금액도 불일치")
    void confirmPayment_StatusNotPaidAndAmountMismatch() {
        // given
        String paymentId = testPayment.getPaymentId();
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId)).thenReturn(Optional.of(testPayment));

        // PortOne API 응답 Mock - 상태도 다르고 금액도 다름
        mockPortOneApiCall("CANCELLED", 5000);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        ConfirmPaymentResponse response = paymentService.confirm(paymentId);

        // then
        assertNotNull(response);
        assertThat(response.getOrderId()).isEqualTo(testOrder.getOrderNumber());
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILURE);

        verify(paymentRepository, times(1)).findFirstByPaymentIdOrderByIdDesc(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 확인 실패 - RestClient 호출 중 예외 발생")
    void confirmPayment_RestClientException() {
        // given
        String paymentId = testPayment.getPaymentId();
        when(paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId))
                .thenReturn(Optional.of(testPayment));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenThrow(new RuntimeException("API 호출 실패"));

        // when & then
        assertThrows(RuntimeException.class, () -> paymentService.confirm(paymentId));
    }

    // ========== 헬퍼 메서드 ==========

    private StartPaymentRequest createStartPaymentRequest(Long orderId) {
        try {
            StartPaymentRequest request = new StartPaymentRequest();
            ReflectionTestUtils.setField(request, "orderId", orderId);
            ReflectionTestUtils.setField(request, "totalAmount", BigDecimal.valueOf(10000.0));
            ReflectionTestUtils.setField(request, "pointsToUse", BigDecimal.valueOf(0.0));
            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PortOnePaymentResponse createPortOneResponse(String status, int totalAmount) {
        PortOnePaymentResponse response = new PortOnePaymentResponse();
        try {
            ReflectionTestUtils.setField(response, "status", status);
            PortOnePaymentResponse.PaymentAmount amount = new PortOnePaymentResponse.PaymentAmount();
            amount.setTotal(totalAmount);
            ReflectionTestUtils.setField(response, "amount", amount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private void mockPortOneApiCall(String status, int amount) {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        PortOnePaymentResponse response = createPortOneResponse(status, amount);
        when(responseSpec.body(PortOnePaymentResponse.class)).thenReturn(response);
    }
}