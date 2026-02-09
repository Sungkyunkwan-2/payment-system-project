package com.paymentteamproject.domain.refund.service;

import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.entity.PaymentStatus;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.refund.dtos.RefundCreateRequest;
import com.paymentteamproject.domain.refund.dtos.RefundCreateResponse;
import com.paymentteamproject.domain.refund.entity.Refund;
import com.paymentteamproject.domain.refund.repository.RefundRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefundCreateResponse requestRefund(Long paymentId, String email, RefundCreateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제를 찾을 수 없습니다."));

        // 소유권 검증
        Long ownerId = payment.getOrder().getUser().getId();
        if (!ownerId.equals(userId)) throw new IllegalArgumentException("해당 결제에 대한 환불 권한이 없습니다.");

        // 멱등성: 이미 refunds 레코드가 있으면 "상태 변경 없이" 그대로 반환(성공/요청중)
        Refund existing = refundRepository.findByPayment_Id(paymentId);
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

        // 환불 레코드 생성(영구 기록: 요청 사실부터 남김)
        double amount = payment.getPrice();
        Refund refund = new Refund(payment, amount, request.getReason());
        refundRepository.save(refund);

        // PortOne 취소 호출 (지금은 Stub)
        boolean cancelSuccess = cancelPortOne(payment);

        // 결과 반영
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
        // 결제 성공건만 환불 가능, 이미 환불된 건은 불가
        return payment.getStatus() == PaymentStatus.SUCCESS;
    }

    private boolean cancelPortOne(Payment payment) {
        // TODO: PortOne 결제 취소 API 연동
        return true;
    }
}