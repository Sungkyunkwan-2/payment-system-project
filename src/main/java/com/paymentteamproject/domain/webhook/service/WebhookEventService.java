package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.domain.webhook.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookEventService {
    private final WebhookEventRepository webhookEventRepository;
}
