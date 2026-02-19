package com.paymentteamproject.domain.webhook.repository;

import com.paymentteamproject.domain.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByWebhookId(String webhookId);
}