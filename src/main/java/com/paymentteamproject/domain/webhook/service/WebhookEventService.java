package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.domain.order.consts.OrderStatus;
import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.order.service.OrderService;
import com.paymentteamproject.domain.payment.entity.Payment;
import com.paymentteamproject.domain.payment.exception.PaymentNotFoundException;
import com.paymentteamproject.domain.payment.repository.PaymentRepository;
import com.paymentteamproject.domain.webhook.consts.PaymentWebhookPaymentStatus;
import com.paymentteamproject.domain.webhook.dto.GetPaymentResponse;
import com.paymentteamproject.domain.webhook.dto.WebHookRequest;
import com.paymentteamproject.domain.webhook.entity.WebhookEvent;
import com.paymentteamproject.domain.webhook.exception.PaymentAmountMismatchException;
import com.paymentteamproject.domain.webhook.exception.PaymentStatusNotAllowedException;
import com.paymentteamproject.domain.webhook.repository.WebhookEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventService {
    private final WebhookEventRepository webhookEventRepository;
    private final PortOneClient portOneClient;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Transactional
    public void processWebhook(String webhookId, @Valid WebHookRequest request) {

        //멱등성 체크
        if (webhookEventRepository.existsByWebhookId(webhookId)) {
            log.info("이미 처리 되었습니다. webhookId: {}", webhookId);
            return;
        }

        //portone 결제조회
        String paymentId = request.getData().getPaymentId();
        GetPaymentResponse portOnePayment = portOneClient.getPayment(paymentId);

        // 동일 paymentId의 최신 이력을 기준으로 다음 상태를 INSERT합니다.
        Payment payment = paymentRepository.findFirstByPaymentIdOrderByIdDesc(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));

        Orders order = payment.getOrder();

        BigDecimal portOneAmount = new BigDecimal(portOnePayment.getAmount().getTotal());
        BigDecimal orderAmount = order.getTotalPrice();

        if (portOneAmount.compareTo(orderAmount) != 0){
            throw  new PaymentAmountMismatchException("결제 금액 불일치");
        }

        //이벤트 기록
        WebhookEvent webhookEvent = new WebhookEvent(
                webhookId,
                request.getData().getPaymentId(),
                portOnePayment.getStatus()
        );

        webhookEventRepository.save(webhookEvent);

        processPaymentStatus(portOnePayment.getStatus(), payment, order);
        webhookEvent.completeProcess();
    }

    private void processPaymentStatus(
            PaymentWebhookPaymentStatus webhookStatus, Payment payment, Orders order) {
        switch (webhookStatus) {
            case PAID -> {
                // success()가 새 엔티티 생성 + order.completedOrder() 내부 호출
                paymentRepository.save(payment.success());
                log.info("[WEBHOOK] 결제 완료 처리 성공 - paymentId: {}, orderId: {}",
                        payment.getPaymentId(), order.getId());
            }

            case FAILED -> {
                paymentRepository.save(payment.fail());
                order.updateStatus(OrderStatus.ORDER_CANCELED);
                log.info("[WEBHOOK] 결제 실패 처리 완료 - paymentId: {}, orderId: {}",
                        payment.getPaymentId(), order.getId());
            }

            case CANCELLED, PARTIAL_CANCELLED -> {
                paymentRepository.save(payment.refund());
                order.updateStatus(OrderStatus.ORDER_CANCELED);
                orderService.processOrderCancellation(order);
                log.info("[WEBHOOK] 환불 처리 완료 - paymentId: {}, orderId: {}, refundType: {}",
                        payment.getPaymentId(), order.getId(), webhookStatus);
            }

            case READY, VIRTUAL_ACCOUNT_ISSUED, PAY_PENDING -> {
                // 최초 생성 시 이미 PENDING — 새 이력 불필요, 주문 상태만 동기화
                order.updateStatus(OrderStatus.PAYMENT_PENDING);
                log.info("[WEBHOOK] 결제 대기 상태 업데이트 - paymentId: {}, orderId: {}, status: {}",
                        payment.getPaymentId(), order.getId(), webhookStatus);
            }

            default -> {
                log.warn("[WEBHOOK] 처리되지 않은 결제 상태 - status: {}", webhookStatus);
                throw new PaymentStatusNotAllowedException("지원하지 않는 결제 상태입니다: " + webhookStatus);
            }
        }
    }
}
