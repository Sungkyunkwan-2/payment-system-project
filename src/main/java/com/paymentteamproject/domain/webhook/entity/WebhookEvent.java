package com.paymentteamproject.domain.webhook.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.webhook.consts.PaymentWebhookPaymentStatus;
import com.paymentteamproject.domain.webhook.consts.WebhookStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@Table(name = "webhook_events")
@NoArgsConstructor
public class WebhookEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String webhookId;

    @Column(nullable = false)
    private String paymentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentWebhookPaymentStatus eventStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column
    private LocalDateTime processedAt;

    @Column
    private LocalDateTime deletedAt;

    public WebhookEvent(String webhookId, String paymentId, PaymentWebhookPaymentStatus eventStatus) {
        this.webhookId = webhookId;
        this.paymentId = paymentId;
        this.eventStatus = eventStatus;
        this.status = WebhookStatus.RECEIVED;
        this.receivedAt = LocalDateTime.now();

        log.info("요청시각: {} 상태: {}", receivedAt, status);
    }

    public void completeProcess() {
        this.status = WebhookStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();

        log.info("처리완료 시각: {}, 상태: {}", processedAt, status);
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
