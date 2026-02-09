package com.paymentteamproject.domain.refund.service;

import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.entity.PaymentStatus;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.refund.dtos.PortOneCancelRequest;
import com.paymentteamproject.domain.refund.dtos.PortOneCancelResponse;
import com.paymentteamproject.domain.refund.dtos.RefundCreateRequest;
import com.paymentteamproject.domain.refund.dtos.RefundCreateResponse;
import com.paymentteamproject.domain.refund.entity.Refund;
import com.paymentteamproject.domain.refund.repository.RefundRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    // RestClientConfig에서 만든 Bean
    private final RestClient portOneRestClient;

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

        // PortOne 취소 호출 (실제 연동)
        boolean cancelSuccess = cancelPortOne(payment, request.getReason());

        // 결과 반영
        if (cancelSuccess) {
            refund.markSuccess(LocalDateTime.now());

            payment.markRefunded();
            payment.getOrder().markRefunded();
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

    private boolean cancelPortOne(Payment payment, String reason) {
        // PortOne 결제 취소는 "PortOne paymentId" (String) 로 호출해야 함
        String portOnePaymentId = payment.getPaymentId();

        try {
            PortOneCancelResponse response = portOneRestClient.post()
                    .uri("/payments/{paymentId}/cancel", portOnePaymentId)
                    .body(new PortOneCancelRequest(reason))
                    .retrieve()
                    .body(PortOneCancelResponse.class);

            // response가 null인 경우도 방어
            return response != null;
        } catch (RestClientResponseException e) {
            return false;
        }
    }
}