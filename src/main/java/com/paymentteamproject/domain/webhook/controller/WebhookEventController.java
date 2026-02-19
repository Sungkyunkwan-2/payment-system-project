package com.paymentteamproject.domain.webhook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentteamproject.domain.webhook.dto.WebHookRequest;
import com.paymentteamproject.domain.webhook.service.WebhookEventService;
import com.paymentteamproject.domain.webhook.webhooksecurity.PortOneWebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookEventController {
    private final WebhookEventService webhookEventService;
    private final PortOneWebhookVerifier verifier;
    private final ObjectMapper objectMapper;

    @PostMapping("/portone-webhook")
    public ResponseEntity<Void> handleWebhookEvent(
            @RequestBody byte[] rawBody,  // 서명 검증용
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature
    ) {
        String rawJson = new String(rawBody, StandardCharsets.UTF_8); log.info("[PORTONE_WEBHOOK] Raw JSON: {}", rawJson);
        boolean verified = verifier.verify(
                rawBody,
                webhookId,
                webhookTimestamp,
                webhookSignature
        );

        log.info("[PORTONE_WEBHOOK] verified: {}", verified);
        if (!verified) {
            log.warn("[PORTONE_WEBHOOK] signature verification failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WebHookRequest request;
        try {
            request = objectMapper.readValue(rawBody, WebHookRequest.class);
            log.info("[PORTONE_WEBHOOK] 파싱 성공 - type: {}, paymentId: {}",
                    request.getType(), request.getData().getPaymentId());
        } catch (Exception e) {
            log.error("[PORTONE_WEBHOOK] JSON 파싱 실패 - webhookId: {}", webhookId, e);
            return ResponseEntity.badRequest().build();
        }

        try {
            webhookEventService.processWebhook(webhookId, request);
            log.info("[PORTONE_WEBHOOK] 처리 완료 - webhookId: {}, paymentId: {}",
                    webhookId, request.getData().getPaymentId());

        } catch (IllegalArgumentException e) {
            log.error("[PORTONE_WEBHOOK] 잘못된 요청 데이터 - webhookId: {}", webhookId, e);
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("[PORTONE_WEBHOOK] 처리 실패 - webhookId: {}", webhookId, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}
