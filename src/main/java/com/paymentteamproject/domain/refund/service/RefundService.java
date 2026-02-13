package com.paymentteamproject.domain.refund.service;

import com.paymentteamproject.domain.order.service.OrderService;
import com.paymentteamproject.domain.payment.consts.PaymentStatus;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.event.TotalSpendChangedEvent;
import com.paymentteamproject.domain.payment.exception.PaymentNotFoundException;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.pointTransaction.service.PointService;
import com.paymentteamproject.domain.refund.dto.PortOneCancelRequest;
import com.paymentteamproject.domain.refund.dto.RefundCreateRequest;
import com.paymentteamproject.domain.refund.dto.RefundCreateResponse;
import com.paymentteamproject.domain.refund.entity.Refund;
import com.paymentteamproject.domain.refund.exception.RefundForbiddenException;
import com.paymentteamproject.domain.refund.exception.RefundInvalidStateException;
import com.paymentteamproject.domain.refund.repository.RefundRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RestClient portOneRestClient;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;
    private final PointService pointService;


    @Transactional
    public RefundCreateResponse requestRefund(String paymentId, String email, RefundCreateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        Payment payment = paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId).orElseThrow(
                () -> new PaymentNotFoundException("해당 결제를 찾을 수 없습니다."));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RefundInvalidStateException("결제 성공 상태만 환불할 수 있습니다.");
        }

        //환불 기간 검증 (1일)
        LocalDateTime paymentTime = payment.getCreatedAt();
        long daysSincePayment = ChronoUnit.DAYS.between(paymentTime, LocalDateTime.now());

        if (daysSincePayment > 1) {
            throw new IllegalStateException("환불 기간(1일)이 지났습니다.");
        }

        // 소유권 검증
        Long ownerId = payment.getOrder().getUser().getId();
        if (!ownerId.equals(userId)) throw new RefundForbiddenException("해당 결제에 대한 환불 권한이 없습니다.");

        // 멱등성: 이미 refunds 레코드가 있으면 "상태 변경 없이" 그대로 반환(성공/요청중)
        Refund latestRefund = refundRepository.findTopByPayment_PaymentIdOrderByIdDesc(paymentId).orElse(null);
        if (latestRefund != null) {
            if (latestRefund.isSuccess()) {
                throw new RefundInvalidStateException("이미 환불 완료된 결제입니다.", HttpStatus.CONFLICT);
            }
            if (latestRefund.isRequesting()) {
                throw new RefundInvalidStateException("이미 환불 처리 중입니다.", HttpStatus.CONFLICT);
            }
        }

        // 환불 요청 이벤트 저장
        BigDecimal amount = payment.getPrice();
        Refund requestEvent = new Refund(payment, amount, request.getReason());
        refundRepository.save(requestEvent);

        // PortOne 취소 호출
        boolean cancelSuccess = cancelPortOne(payment, request.getReason());

        if (cancelSuccess) {
            Refund successEvent = requestEvent.success(LocalDateTime.now());
            refundRepository.save(successEvent);

            payment.getOrder().markRefunded();
            orderService.processOrderCancellation(payment.getOrder());

            Payment refundedPayment = paymentRepository.save(payment.refund());

            //포인트 복구
            BigDecimal usedPoint = refundedPayment.getOrder().getUsedPoint();
            if (usedPoint != null && usedPoint.compareTo(BigDecimal.ZERO) > 0) {
                pointService.refundPoints(
                        refundedPayment.getOrder().getUser(),
                        refundedPayment.getOrder(),
                        usedPoint
                );
            } else {
                // 포인트 미사용 결제: 적립 포인트 회수만
                pointService.refundPointsForNoPointPayment(
                        refundedPayment.getOrder().getUser(),
                        refundedPayment.getOrder()
                );
            }

            // 총 거래액 이벤트
            BigDecimal price = refundedPayment.getPrice();
            BigDecimal negatedPrice = (price != null) ? price.negate() : BigDecimal.ZERO;

            eventPublisher.publishEvent(new TotalSpendChangedEvent(
                    refundedPayment.getOrder().getUser(),
                    negatedPrice
            ));

            return toResponse(successEvent);
        }

        Refund failureEvent = requestEvent.failure();
        refundRepository.save(failureEvent);
        return toResponse(failureEvent);
    }

    private RefundCreateResponse toResponse(Refund refund) {
        return new RefundCreateResponse(
                refund.getId(),
                refund.getPayment().getId().toString(),
                refund.getAmount(),
                refund.getStatus(),
                refund.getReason(),
                refund.getRefundedAt()
        );
    }

    private boolean cancelPortOne(Payment payment, String reason) {
        String portOnePaymentId = payment.getPaymentId();

        try {
            portOneRestClient.post()
                    .uri("/payments/{paymentId}/cancel", portOnePaymentId)
                    .body(new PortOneCancelRequest(reason))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException e) {
            log.error("PortOne cancel failed. paymentId={}, status={}, body={}",
                    portOnePaymentId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return false;
        }
    }
}