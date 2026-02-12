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
import com.paymentteamproject.domain.payment.exception.*;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.pointTransaction.exception.ExcessivePointUsageException;
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

        User user = order.getUser();
        BigDecimal pointsToUse = request.getPointsToUse();

        //포인트 미사용 시
        if (pointsToUse == null || pointsToUse.compareTo(BigDecimal.ZERO) == 0) {
            Payment payment = Payment.start(order, request.getTotalAmount());
            Payment savedPayment = paymentRepository.save(payment);

            return new StartPaymentResponse(
                    savedPayment.getPaymentId(),
                    savedPayment.getStatus(),
                    savedPayment.getCreatedAt());
        }

        //포인트를 이용한 결제
        return processPaymentWithPoints(order, user, request.getTotalAmount(), pointsToUse);
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
            Orders order = payment.getOrder();
            User user = order.getUser();
            BigDecimal usedPoints = order.getUsedPoint();

            // 포인트 차감 수행
            if (usedPoints != null && usedPoints.compareTo(BigDecimal.ZERO) > 0) {
                pointService.usePoints(user, order, usedPoints);
            }

            Payment success = payment.success();
            Payment savedSuccess = paymentRepository.save(success);

            // 총 거래액 이벤트 발행
            eventPublisher.publishEvent(
                    new TotalSpendChangedEvent(user, savedSuccess.getPrice())
            );

            // 포인트 적립
            pointService.applyEarnedPoints(user, order);

            return new ConfirmPaymentResponse(
                    savedSuccess.getOrder().getOrderNumber(),
                    savedSuccess.getStatus());

        } catch (Exception e) {

            refundService.requestRefund(
                    payment.getPaymentId(),
                    payment.getOrder().getUser().getEmail(),
                    new RefundCreateRequest());

            throw new PaymentCompensationException(
                    "결제 승인 처리 중 내부 오류로 인해 결제 취소되었습니다.");
        }
    }


    //포인트를 사용한 통합 결제 처리
    private StartPaymentResponse processPaymentWithPoints(
            Orders order, User user, BigDecimal totalAmount, BigDecimal pointsToUse) {

        // 포인트 검증
        if (pointsToUse.compareTo(totalAmount) > 0) {
            throw new ExcessivePointUsageException("사용 포인트가 주문 금액을 초과할 수 없습니다.");
        }

        BigDecimal actualPaymentAmount = totalAmount.subtract(pointsToUse);
        order.setUsedPoint(pointsToUse);

        // 2. 결제 금액 검증
        if (actualPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPaymentAmountException("결제 금액이 음수일 수 없습니다.");
        }

        // 3. Payment 엔티티 생성
        Payment payment = Payment.start(order, actualPaymentAmount);
        Payment savedPayment = paymentRepository.save(payment);

        // 4. 전액 포인트 결제인 경우 (실제 결제 금액 = 0원)
        if (actualPaymentAmount.compareTo(BigDecimal.ZERO) == 0) {
            return handleFullPointPayment(savedPayment, user, order);
        }

        // 5. 포인트 사용 후 금액이 1000원 미만일 경우
        if (actualPaymentAmount.compareTo(BigDecimal.valueOf(1000)) < 0) {
            throw new MinimumPaymentAmountException("결제 금액은 1000원 미만일 수 없습니다.");
        }

        // 6. 부분 포인트 결제인 경우
        return handlePartialPointPayment(savedPayment);
    }


    //전액 포인트 결제 처리 (결제 금액 0원)
    private StartPaymentResponse handleFullPointPayment(
            Payment payment, User user, Orders order) {

        try {
            // 결제 성공 처리 (PortOne 호출 없이 바로 SUCCESS)
            Payment success = payment.success();
            Payment savedSuccess = paymentRepository.save(success);

            // 결제 성공 시 총 거래액 변동 이벤트 발행, 비동기 처리
            eventPublisher.publishEvent(new TotalSpendChangedEvent(
                    savedSuccess.getOrder().getUser(),
                    savedSuccess.getPrice())
            );

            return new StartPaymentResponse(
                    savedSuccess.getPaymentId(),
                    savedSuccess.getStatus(),
                    savedSuccess.getCreatedAt());

        } catch (Exception e) {
            throw new PaymentCompensationException("결제 처리 중 내부 오류가 발생했습니다.");
        }
    }

    //부분 포인트 결제 처리
    private StartPaymentResponse handlePartialPointPayment(
            Payment payment) {

        try {
            return new StartPaymentResponse(
                    payment.getPaymentId(),
                    payment.getStatus(),
                    payment.getCreatedAt());

        } catch (Exception e) {
            throw new PaymentCompensationException("결제 시작 처리 중 내부 오류가 발생했습니다.");
        }
    }
}
