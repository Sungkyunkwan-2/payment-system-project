package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.domain.webhook.dto.WebHookRequest;
import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import com.paymentteamproject.domain.webhook.entity.WebhookEvent;
import com.paymentteamproject.domain.webhook.repository.WebhookEventRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

        PaymentWebhookPaymentStatus eventStatus = PaymentWebhookPaymentStatus.valueOf(request.getData().getStatus());

        //이벤트 기록
        WebhookEvent webhookEvent = new WebhookEvent(
                webhookId,
                request.getData().getPaymentId(),
                eventStatus
        );
        webhookEventRepository.save(webhookEvent);

        //처리
        try {
            //재고처리 어떻게


            webhookEvent.completeProcess();
            log.info("처리 완료. webhookId: {}, paymentId: {}",
                    webhookId, request.getData().getPaymentId());

        } catch (Exception e) {
            webhookEvent.fail();
            log.error("처리 중 오류 발생. webhookId: {}, paymentId: {}",
                    webhookId, request.getData().getPaymentId(), e);
            throw e;
        }
    }
}
