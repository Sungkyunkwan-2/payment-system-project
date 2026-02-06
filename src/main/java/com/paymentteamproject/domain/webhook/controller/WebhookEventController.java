package com.paymentteamproject.domain.webhook.controller;

import com.paymentteamproject.domain.webhook.dto.WebHookRequest;
import com.paymentteamproject.domain.webhook.entity.WebhookEvent;
import com.paymentteamproject.domain.webhook.service.WebhookEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookEventController {
    private final WebhookEventService webhookEventService;

    @PostMapping("/portone-webhook")
    public ResponseEntity<Void> handleWebhookEvent(@Valid @RequestBody WebHookRequest request) {
        webhookEventService.processWebhook(request);

        return ResponseEntity.ok().build();
    }
}
