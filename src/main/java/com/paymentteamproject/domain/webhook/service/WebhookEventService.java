package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.domain.webhook.dto.WebHookRequest;
import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import com.paymentteamproject.domain.webhook.entity.WebhookEvent;
import com.paymentteamproject.domain.webhook.repository.WebhookEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventService {
    private final WebhookEventRepository webhookEventRepository;

    @Transactional
    public void processWebhook(String webhookId, @Valid WebHookRequest request) {

        //멱등성 체크
        if (webhookEventRepository.existsByWebhookId(webhookId)) {
            log.info("이미 처리 되었습니다. webhookId: {}", webhookId);
            return;
        }



        //이벤트 기록
        WebhookEvent webhookEvent = new WebhookEvent(
                webhookId,
                request.getData().getPaymentId(),
                request.getData().getStatus()
        );
        webhookEventRepository.save(webhookEvent);

        try {
            //Payment payment = PortOneClient.getPayment(request.getData().getPaymentId());

            switch (webhookEvent.getEventStatus()) {
                case PAID:
                    handlePaidEvent(request.getData().getPaymentId());
                    break;

                case CANCELLED:
                case PARTIAL_CANCELLED:
                    handleCancelEvent(request.getData().getPaymentId(), request.getData().getStatus());
                    break;

                case FAILED:
                    handleFailedEvent(request.getData().getPaymentId());
                    break;

                case READY:
                case VIRTUAL_ACCOUNT_ISSUED:
                case PAY_PENDING:
                    handlePendingEvent(request.getData().getPaymentId(), request.getData().getStatus());
                    break;

                default:
                    log.warn("처리되지 않은 상태 - status: {}", request.getData().getStatus());
            }

            webhookEvent.completeProcess();
            log.info("Webhook 처리 완료 - webhookId: {}, paymentId: {}",
                    webhookId, request.getData().getPaymentId());

        } catch (Exception e) {
            webhookEvent.fail();
            log.error("Webhook 처리 중 오류 발생 - webhookId: {}, paymentId: {}",
                    webhookId, request.getData().getPaymentId(), e);
            throw new RuntimeException("Webhook 처리 실패", e);
        }
    }

    private void handlePaidEvent(String paymentId) {
        log.info("결제 완료 처리 시작 - paymentId: {}", paymentId);

        // 주문 상태를 '결제완료'로 변경
        // order.confirmPayment(paymentId);

        // 재고 차감
        // product.decreaseStock(order.getItems());

        // 주문 확정 처리
        // order.confirm(orderId);

        log.info("결제 완료 처리 완료 - paymentId: {}", paymentId);
    }

    private void handleCancelEvent(String paymentId, PaymentWebhookPaymentStatus status) {
        log.info("취소 처리 시작 - paymentId: {}, status: {}", paymentId, status);

        // 주문 상태를 취소됨으로 변경
        // order.cancel(paymentId, status);

        // 재고 복구
        // product.restoreStock(order.getItems());

        // 환불 처리 기록
        // refund.record(paymentId, status);

        log.info("취소 처리 완료 - paymentId: {}", paymentId);
    }

    private void handleFailedEvent(String paymentId) {
        log.info("결제 실패 처리 - paymentId: {}", paymentId);

        // 주문 상태를 '결제실패'로 변경
        // order.markAsFailed(paymentId);
    }

    private void handlePendingEvent(String paymentId, PaymentWebhookPaymentStatus status) {
        log.info("대기 상태 처리 - paymentId: {}, status: {}", paymentId, status);

        // 주문 상태 업데이트
        // order.updateStatus(paymentId, status);
    }
}



