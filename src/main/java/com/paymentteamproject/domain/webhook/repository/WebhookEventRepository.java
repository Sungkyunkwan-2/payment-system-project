package com.paymentteamproject.domain.webhook.repository;

import com.paymentteamproject.domain.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Optional<WebhookEvent> findByWebhookId(String id);
    boolean existsByWebhookId(String id);
}
