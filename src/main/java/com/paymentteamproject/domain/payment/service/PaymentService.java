package com.paymentteamproject.domain.payment.service;

import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.exception.OrderNotFoundException;
import com.paymentteamproject.domain.order.repository.OrderRepository;
import com.paymentteamproject.domain.payment.dto.ConfirmPaymentResponse;
import com.paymentteamproject.domain.payment.dto.PortOnePaymentResponse;
import com.paymentteamproject.domain.payment.dto.StartPaymentRequest;
import com.paymentteamproject.domain.payment.dto.StartPaymentResponse;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.consts.PaymentStatus;
import com.paymentteamproject.domain.payment.event.TotalSpendChangedEvent;
import com.paymentteamproject.domain.payment.exception.DuplicatePaymentConfirmException;
import com.paymentteamproject.domain.payment.exception.PaymentCompensationException;
import com.paymentteamproject.domain.payment.exception.PaymentNotFoundException;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.pointTransaction.service.PointService;
import com.paymentteamproject.domain.refund.dto.RefundCreateRequest;
import com.paymentteamproject.domain.refund.service.RefundService;
import com.paymentteamproject.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestClient restClient;
    private final RefundService refundService;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;

        // 결제 시작
    @Transactional
    public StartPaymentResponse start(StartPaymentRequest request) {

        Orders order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new OrderNotFoundException("존재하지 않는 주문입니다"));

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

        Payment payment = paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId).orElseThrow(
                () -> new PaymentNotFoundException("존재하지 않는 결제입니다."));

        if(payment.getStatus().equals(PaymentStatus.SUCCESS)) {
            throw new DuplicatePaymentConfirmException("이미 처리 중이거나 완료된 결제입니다.");
        }

        // PortOne API 결제 조회 값 불러오기
        PortOnePaymentResponse response = restClient.get()
                .uri("payments/{paymentId}", paymentId)
                .retrieve()
                .body(PortOnePaymentResponse.class);

        if(response == null) throw new PaymentNotFoundException("존재하지 않는 결제입니다.");

        int amount = response.getAmount().getTotal();
        if(!response.getStatus().equals("PAID") || payment.getPrice().compareTo(new BigDecimal(amount)) != 0) {
            Payment fail = payment.fail();
            Payment savedFail = paymentRepository.save(fail);

            return new ConfirmPaymentResponse(
                    savedFail.getOrder().getOrderNumber(),
                    savedFail.getStatus());
        }

        try {
            Payment success = payment.success();
            Payment savedSuccess = paymentRepository.save(success);

            // 결제 성공 시 총 거래액 변동 이벤트 발행, 비동기 처리
            eventPublisher.publishEvent(new TotalSpendChangedEvent(
                    savedSuccess.getOrder().getUser(),
                    savedSuccess.getPrice())
            );

            // 결제 성공 시 포인트 적립 추가
            Orders order = savedSuccess.getOrder();
            User user = order.getUser();
            pointService.applyEarnedPoints(user, order);

            return new ConfirmPaymentResponse(
                    savedSuccess.getOrder().getOrderNumber(),
                    savedSuccess.getStatus());
        } catch (Exception e) {
            // TODO 결제 취소 메서드 호출
            refundService.requestRefund(
                    payment.getPaymentId(),
                    payment.getOrder().getUser().getEmail(),
                    new RefundCreateRequest());
            throw new PaymentCompensationException("결제 승인 처리 중 내부 오류로 인해 결제 취소되었습니다.");
        }
    }
}
