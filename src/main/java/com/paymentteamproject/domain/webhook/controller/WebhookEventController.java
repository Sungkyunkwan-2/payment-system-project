package com.paymentteamproject.domain.webhook.controller;

import com.paymentteamproject.domain.webhook.service.WebhookEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WebhookEventController {
    private final WebhookEventService webhookEventService;
}
