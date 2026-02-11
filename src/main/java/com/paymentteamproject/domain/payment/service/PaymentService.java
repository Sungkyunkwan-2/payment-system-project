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
import com.paymentteamproject.domain.payment.exception.DuplicatePaymentConfirmException;
import com.paymentteamproject.domain.payment.exception.PaymentCompensationException;
import com.paymentteamproject.domain.payment.exception.PaymentNotFoundException;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.pointTransaction.service.PointService;
import com.paymentteamproject.domain.refund.dto.RefundCreateRequest;
import com.paymentteamproject.domain.refund.service.RefundService;
import com.paymentteamproject.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
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

        // 결제 시작
    @Transactional
    public StartPaymentResponse start(StartPaymentRequest request) {

        Orders order = orderRepository.findById(request.getOrderId()).orElseThrow(
                () -> new OrderNotFoundException("존재하지 않는 주문입니다"));

        User user = order.getUser();
        BigDecimal pointsToUse = request.getPointsToUse();

        // 2. 포인트 미사용 시 - 기존 로직 (PENDING 상태로 반환)
        if (pointsToUse == null || pointsToUse.compareTo(BigDecimal.ZERO) == 0) {
            Payment payment = Payment.start(order, request.getTotalAmount());
            Payment savedPayment = paymentRepository.save(payment);

            return new StartPaymentResponse(
                    savedPayment.getPaymentId(),
                    savedPayment.getStatus(),
                    savedPayment.getCreatedAt());
        }

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
            Payment success = payment.success();
            Payment savedSuccess = paymentRepository.save(success);

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


    /**
     * 포인트를 사용한 통합 결제 처리
     */
    private StartPaymentResponse processPaymentWithPoints(
            Orders order, User user, BigDecimal totalAmount, BigDecimal pointsToUse) {

        // 1. 포인트 검증 및 차감
        BigDecimal actualPaymentAmount;
        try {
            // 포인트 사용 금액이 총 금액을 초과하는지 검증
            if (pointsToUse.compareTo(totalAmount) > 0) {
                throw new IllegalStateException("사용 포인트가 주문 금액을 초과할 수 없습니다.");
            }

            // 포인트 차감 (잔액 부족 시 예외 발생)
            pointService.usePoints(user, pointsToUse);

            // 실제 결제 금액 계산
            actualPaymentAmount = totalAmount.subtract(pointsToUse);

            // 주문에 사용 포인트 기록
            order.setUsedPoint(pointsToUse);

        } catch (Exception e) {
            throw new IllegalStateException("포인트 사용에 실패했습니다: " + e.getMessage());
        }

        // 2. 결제 금액 검증
        if (actualPaymentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("결제 금액이 음수일 수 없습니다.");
        }

        // 3. Payment 엔티티 생성
        Payment payment = Payment.start(order, actualPaymentAmount);
        Payment savedPayment = paymentRepository.save(payment);

        // 4. 전액 포인트 결제인 경우 (실제 결제 금액 = 0원)
        if (actualPaymentAmount.compareTo(BigDecimal.ZERO) == 0) {
            return handleFullPointPayment(savedPayment, user, order, pointsToUse);
        }

        // 5. 부분 포인트 결제인 경우 (PortOne API 호출)
        return handlePartialPointPayment(savedPayment, user, order, pointsToUse, actualPaymentAmount);
    }


    //전액 포인트 결제 처리 (결제 금액 0원)
    private StartPaymentResponse handleFullPointPayment(
            Payment payment, User user, Orders order, BigDecimal usedPoints) {

        try {
            // 결제 성공 처리 (PortOne 호출 없이 바로 SUCCESS)
            Payment success = payment.success();
            Payment savedSuccess = paymentRepository.save(success);

            // 포인트 적립
            pointService.applyEarnedPoints(user, order);

            return new StartPaymentResponse(
                    savedSuccess.getPaymentId(),
                    savedSuccess.getStatus(),
                    savedSuccess.getCreatedAt());

        } catch (Exception e) {
            // 보상 트랜잭션: 포인트 복구
            compensatePointUsage(user, usedPoints);
            throw new PaymentCompensationException("결제 처리 중 내부 오류가 발생했습니다.");
        }
    }

    /**
     * 부분 포인트 결제 처리 (PortOne API 호출 필요)
     */
    private StartPaymentResponse handlePartialPointPayment(
            Payment payment, User user, Orders order,
            BigDecimal usedPoints, BigDecimal actualPaymentAmount) {

        try {
            // PortOne API 결제 확인
            PortOnePaymentResponse response = restClient.get()
                    .uri("payments/{paymentId}", payment.getPaymentId())
                    .retrieve()
                    .body(PortOnePaymentResponse.class);

            if (response == null) {
                throw new PaymentNotFoundException("결제 정보를 조회할 수 없습니다.");
            }

            // 결제 검증
            int paidAmount = response.getAmount().getTotal();
            if (!response.getStatus().equals("PAID") ||
                    actualPaymentAmount.compareTo(new BigDecimal(paidAmount)) != 0) {
                // 결제 실패 처리
                Payment fail = payment.fail();
                paymentRepository.save(fail);

                // 보상 트랜잭션: 포인트 복구
                compensatePointUsage(user, usedPoints);

                throw new IllegalStateException("결제 금액 또는 상태가 일치하지 않습니다.");
            }

            // 결제 성공 처리
            Payment success = payment.success();
            Payment savedSuccess = paymentRepository.save(success);

            // 포인트 적립
            pointService.applyEarnedPoints(user, order);

            return new StartPaymentResponse(
                    savedSuccess.getPaymentId(),
                    savedSuccess.getStatus(),
                    savedSuccess.getCreatedAt());

        } catch (PaymentNotFoundException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {

            // 보상 트랜잭션
            try {
                // PortOne 결제 취소
                refundService.requestRefund(
                        payment.getPaymentId(),
                        user.getEmail(),
                        new RefundCreateRequest());

                // 포인트 복구
                compensatePointUsage(user, usedPoints);

            } catch (Exception compensationError) {
            }

            throw new PaymentCompensationException("결제 승인 처리 중 내부 오류로 인해 결제가 취소되었습니다.");
        }
    }

    /**
     * 포인트 복구 (보상 트랜잭션)
     */
    private void compensatePointUsage(User user, BigDecimal pointsToUse) {
        if (pointsToUse != null && pointsToUse.compareTo(BigDecimal.ZERO) > 0) {
            try {
                pointService.refundPoints(user, pointsToUse);
            } catch (Exception e) {
               throw new IllegalStateException("", e);
            }
        }
    }
}
