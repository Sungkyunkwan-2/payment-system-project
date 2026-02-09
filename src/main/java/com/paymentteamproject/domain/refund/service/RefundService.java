package com.paymentteamproject.domain.refund.service;

import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.refund.dtos.RefundCreateRequest;
import com.paymentteamproject.domain.refund.dtos.RefundCreateResponse;
import com.paymentteamproject.domain.refund.entity.Refund;
import com.paymentteamproject.domain.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public RefundCreateResponse requestRefund(Long paymentId, Long userId, RefundCreateRequest request) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제를 찾을 수 없습니다."));

        // 소유권 검증(팀 Security 구조에 맞춰 적용)
        // Long ownerId = payment.getOrder().getUser().getId();
        // if (!ownerId.equals(userId)) throw new RefundBadRequestException("해당 결제에 대한 환불 권한이 없습니다.");

        // 멱등성: 이미 refunds 레코드가 있으면 "상태 변경 없이" 그대로 반환(성공/요청중)
        Refund existing = refundRepository.findByPaymentId(paymentId);
        if (existing != null) {
            if (existing.isSuccess() || existing.isRequesting()) {
                return toResponse(existing);
            }
            throw new IllegalArgumentException("환불 실패 상태입니다.");
        }

        // 환불 가능 상태 검증
        if (!isRefundable(payment)) {
            throw new IllegalArgumentException("현재 상태에서는 환불이 불가능합니다.");
        }

        double amount = payment.getPrice();

        Refund refund = new Refund(payment, amount, request.getReason());
        refundRepository.save(refund);

        // 2) PortOne 취소 호출 (지금은 Stub)
        boolean cancelSuccess = cancelPortOne(payment);

        // 3) 결과 반영
        if (cancelSuccess) {
            refund.markSuccess(LocalDateTime.now());

            // TODO: payment.markRefunded();
            // TODO: payment.getOrder().markRefunded();
            // TODO: 포인트 복구 / 적립 취소 / 멤버십 재계산

            return toResponse(refund);
        }

        refund.markFailure();
        throw new IllegalArgumentException("환불 처리 중 오류가 발생했습니다.");
    }

    // DTO static 없이 매핑
    private RefundCreateResponse toResponse(Refund refund) {
        return new RefundCreateResponse(
                refund.getId(),
                refund.getPayment().getId(),
                refund.getAmount(),
                refund.getStatus(),
                refund.getReason(),
                refund.getRefundedAt()
        );
    }

    private boolean isRefundable(Payment payment) {
        // TODO: 팀 결제/주문 상태 enum에 맞게 정확히 구현

        return true; // 임시
    }

    private boolean cancelPortOne(Payment payment) {
        // TODO: PortOne 결제 취소 API 연동
        return true;
    }
}