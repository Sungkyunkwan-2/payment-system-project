package com.paymentteamproject.domain.payment.service;

import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.payment.dtos.ConfirmPaymentResponse;
import com.paymentteamproject.domain.payment.dtos.PortOnePaymentResponse;
import com.paymentteamproject.domain.payment.dtos.StartPaymentRequest;
import com.paymentteamproject.domain.payment.dtos.StartPaymentResponse;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestClient restClient;

        // 결제 시작
    @Transactional
    public StartPaymentResponse start(StartPaymentRequest request) {

        Orders order = orderRepository.findById(request.getOrderId()).orElseThrow(
                // TODO 주문 예외 협업
                () -> new IllegalArgumentException("존재하지 않는 주문입니다"));

        // TODO payedAt 결제창 후? or initPayment 생성 시 (후자의 경우 createdAt과의 차별점?)
        Payment payment = Payment.start(order, request.getTotalAmount());
        // TODO request.getPointsToUse 포인트 미구현으로 누락

        Payment savedPayment = paymentRepository.save(payment);

        return new StartPaymentResponse(
                savedPayment.getPaymentId(),
                savedPayment.getStatus(),
                savedPayment.getCreatedAt());
    }

    // 결제 확인
    @Transactional
    public ConfirmPaymentResponse confirm(String paymentId) {

        Payment payment = paymentRepository.findByPaymentId(paymentId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 결제입니다."));

        // PortOne API 결제 조회 값 불러오기
        PortOnePaymentResponse response = restClient.get()
                .uri("payments/{paymentId}", paymentId)
                .retrieve()
                .body(PortOnePaymentResponse.class);

        if(response == null) throw new IllegalArgumentException("존재하지 않는 결제입니다.");

        int amount = response.getAmount().getTotal();
        if(!response.getStatus().equals("PAID") || amount != payment.getPrice()) {
            Payment fail = payment.fail();
            Payment savedFail = paymentRepository.save(fail);

            return new ConfirmPaymentResponse(
                    savedFail.getOrder().getOrderNumber(),
                    savedFail.getStatus());
        }

        Payment success = payment.success();
        Payment savedSuccess = paymentRepository.save(success);
        // TODO 재고 차감 및 주문 상태 변경

        return new ConfirmPaymentResponse(
                savedSuccess.getOrder().getOrderNumber(),
                savedSuccess.getStatus());
    }
}
