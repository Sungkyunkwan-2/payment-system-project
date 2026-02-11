//package com.paymentteamproject.domain.payment.service;
//
//import com.paymentteamproject.domain.order.consts.OrderStatus;
//import com.paymentteamproject.domain.order.entity.Orders;
//import com.paymentteamproject.domain.order.exception.OrderNotFoundException;
//import com.paymentteamproject.domain.order.repository.OrderRepository;
//import com.paymentteamproject.domain.payment.dto.ConfirmPaymentResponse;
//import com.paymentteamproject.domain.payment.dto.PortOnePaymentResponse;
//import com.paymentteamproject.domain.payment.dto.StartPaymentRequest;
//import com.paymentteamproject.domain.payment.dto.StartPaymentResponse;
//import com.paymentteamproject.domain.payment.entity.Payment;
//import com.paymentteamproject.domain.payment.consts.PaymentStatus;
//import com.paymentteamproject.domain.payment.exeception.PaymentNotFoundException;
//import com.paymentteamproject.domain.payment.repository.PaymentRepository;
//import com.paymentteamproject.domain.user.entity.User;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.web.client.RestClient;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PaymentServiceTest {
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private RestClient restClient;
//
//    @Mock
//    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
//
//    @Mock
//    private RestClient.ResponseSpec responseSpec;
//
//    @InjectMocks
//    private PaymentService paymentService;
//
//    private User testUser;
//    private Orders testOrder;
//    private Payment testPayment;
//
//    @BeforeEach
//    void setUp() {
//        // 테스트 사용자 생성
//        testUser = User.builder()
//                .username("테스트유저")
//                .phone("01012345678")
//                .email("test@example.com")
//                .password("password123")
//                .pointBalance(5000.0)
//                .build();
//
//        // 테스트 주문 생성
//        testOrder = Orders.builder()
//                .user(testUser)
//                .orderNumber(120260209150134L)
//                .totalPrice(10000.0)
//                .usedPoint(0.0)
//                .status(OrderStatus.PAYMENT_PENDING)
//                .build();
//        // Reflection으로 ID 설정
//        setField(testOrder, "id", 1L);
//
//        // 테스트 결제 생성
//        testPayment = Payment.start(testOrder, 10000.0);
//        setField(testPayment, "id", 1L);
//        setField(testPayment, "createdAt", LocalDateTime.now());
//    }
//
//    // Reflection 헬퍼 메서드
//    private void setField(Object target, String fieldName, Object value) {
//        try {
//            var field = target.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            field.set(target, value);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    // ========== 결제 시작 테스트 ==========
//
//    @Test
//    @DisplayName("결제 시작 성공")
//    void startPayment_Success() {
//        // given
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
//        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
//            Payment payment = invocation.getArgument(0);
//            setField(payment, "id", 1L);
//            setField(payment, "createdAt", LocalDateTime.now());
//            return payment;
//        });
//
//        // when
//        StartPaymentResponse response = paymentService.start(
//                createStartPaymentRequest(1L, 10000.0, 0.0)
//        );
//
//        // then
//        assertNotNull(response);
//        assertTrue(response.getPaymentId().startsWith("PAY1"));
//        assertEquals(PaymentStatus.PENDING, response.getStatus());
//        assertNotNull(response.getCreatedAt());
//
//        verify(orderRepository, times(1)).findById(1L);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    @Test
//    @DisplayName("결제 시작 실패 - 존재하지 않는 주문")
//    void startPayment_OrderNotFound() {
//        // given
//        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // when & then
//        OrderNotFoundException exception = assertThrows(
//                OrderNotFoundException.class,
//                () -> paymentService.start(createStartPaymentRequest(999L, 10000.0, 0.0))
//        );
//
//        assertEquals("존재하지 않는 주문입니다", exception.getMessage());
//        verify(orderRepository, times(1)).findById(999L);
//        verify(paymentRepository, never()).save(any(Payment.class));
//    }
//
//    // ========== 결제 확인 테스트 ==========
//
//    @Test
//    @DisplayName("결제 확인 성공 - PAID 상태이고 금액 일치")
//    void confirmPayment_Success() {
//        // given
//        String paymentId = testPayment.getPaymentId();
//        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(testPayment));
//
//        // RestClient Mock 설정
//        when(restClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
//
//        // PortOne API 응답 Mock
//        PortOnePaymentResponse portOneResponse = createPortOneResponse("PAID", 10000);
//        when(responseSpec.body(PortOnePaymentResponse.class)).thenReturn(portOneResponse);
//
//        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        ConfirmPaymentResponse response = paymentService.confirm(paymentId);
//
//        // then
//        assertNotNull(response);
//        assertEquals(testOrder.getId(), response.getOrderId());
//        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
//
//        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    @Test
//    @DisplayName("결제 확인 실패 - 존재하지 않는 결제")
//    void confirmPayment_PaymentNotFound() {
//        // given
//        String paymentId = "INVALID_PAY_ID";
//        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());
//
//        // when & then
//        PaymentNotFoundException exception = assertThrows(
//                PaymentNotFoundException.class,
//                () -> paymentService.confirm(paymentId)
//        );
//
//        assertEquals("존재하지 않는 결제입니다.", exception.getMessage());
//        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
//        verify(restClient, never()).get();
//    }
//
//    @Test
//    @DisplayName("결제 확인 실패 - PortOne API 응답 null")
//    void confirmPayment_PortOneResponseNull() {
//        // given
//        String paymentId = testPayment.getPaymentId();
//        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(testPayment));
//
//        // RestClient Mock 설정
//        when(restClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.body(PortOnePaymentResponse.class)).thenReturn(null);
//
//        // when & then
//        PaymentNotFoundException exception = assertThrows(
//                PaymentNotFoundException.class,
//                () -> paymentService.confirm(paymentId)
//        );
//
//        assertEquals("존재하지 않는 결제입니다.", exception.getMessage());
//        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
//    }
//
//    @Test
//    @DisplayName("결제 확인 실패 - 상태가 PAID가 아님")
//    void confirmPayment_StatusNotPaid() {
//        // given
//        String paymentId = testPayment.getPaymentId();
//        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(testPayment));
//
//        // RestClient Mock 설정
//        when(restClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
//
//        // PortOne API 응답 Mock - 상태가 READY
//        PortOnePaymentResponse portOneResponse = createPortOneResponse("READY", 10000);
//        when(responseSpec.body(PortOnePaymentResponse.class)).thenReturn(portOneResponse);
//
//        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        ConfirmPaymentResponse response = paymentService.confirm(paymentId);
//
//        // then
//        assertNotNull(response);
//        assertEquals(testOrder.getId(), response.getOrderId());
//        assertEquals(PaymentStatus.FAILURE, response.getStatus());
//
//        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    @Test
//    @DisplayName("결제 확인 실패 - 금액 불일치")
//    void confirmPayment_AmountMismatch() {
//        // given
//        String paymentId = testPayment.getPaymentId();
//        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(testPayment));
//
//        // RestClient Mock 설정
//        when(restClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
//
//        // PortOne API 응답 Mock - 금액 불일치
//        PortOnePaymentResponse portOneResponse = createPortOneResponse("PAID", 5000);
//        when(responseSpec.body(PortOnePaymentResponse.class)).thenReturn(portOneResponse);
//
//        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        ConfirmPaymentResponse response = paymentService.confirm(paymentId);
//
//        // then
//        assertNotNull(response);
//        assertEquals(testOrder.getId(), response.getOrderId());
//        assertEquals(PaymentStatus.FAILURE, response.getStatus());
//
//        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    @Test
//    @DisplayName("결제 확인 실패 - 상태가 PAID가 아니면서 금액도 불일치")
//    void confirmPayment_StatusNotPaidAndAmountMismatch() {
//        // given
//        String paymentId = testPayment.getPaymentId();
//        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(testPayment));
//
//        // RestClient Mock 설정
//        when(restClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
//
//        // PortOne API 응답 Mock - 상태도 다르고 금액도 다름
//        PortOnePaymentResponse portOneResponse = createPortOneResponse("CANCELLED", 5000);
//        when(responseSpec.body(PortOnePaymentResponse.class)).thenReturn(portOneResponse);
//
//        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        ConfirmPaymentResponse response = paymentService.confirm(paymentId);
//
//        // then
//        assertNotNull(response);
//        assertEquals(testOrder.getId(), response.getOrderId());
//        assertEquals(PaymentStatus.FAILURE, response.getStatus());
//
//        verify(paymentRepository, times(1)).findByPaymentId(paymentId);
//        verify(paymentRepository, times(1)).save(any(Payment.class));
//    }
//
//    // ========== 헬퍼 메서드 ==========
//
//    private StartPaymentRequest createStartPaymentRequest(Long orderId, double totalAmount, double pointsToUse) {
//        try {
//            StartPaymentRequest request = new StartPaymentRequest();
//            setField(request, "orderId", orderId);
//            setField(request, "totalAmount", totalAmount);
//            setField(request, "pointsToUse", pointsToUse);
//            return request;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private PortOnePaymentResponse createPortOneResponse(String status, int totalAmount) {
//        PortOnePaymentResponse response = new PortOnePaymentResponse();
//        try {
//            setField(response, "status", status);
//            PortOnePaymentResponse.PaymentAmount amount = new PortOnePaymentResponse.PaymentAmount();
//            amount.setTotal(totalAmount);
//            setField(response, "amount", amount);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return response;
//    }
//}