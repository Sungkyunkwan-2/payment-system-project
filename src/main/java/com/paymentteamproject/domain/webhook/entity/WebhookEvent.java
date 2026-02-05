package com.paymentteamproject.domain.webhook.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "webhook_events")
@NoArgsConstructor
public class WebhookEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    //portone에서 전달값
    @Column(nullable = false, unique = true)
    private String webhookId;

    @Column(nullable = false)
    private String paymentId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentWebhookPaymentStatus eventStatus;

    //webhook 시스템
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WebhookStatus status;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    @Column
    private LocalDateTime deletedAt;

    public WebhookEvent(String webhookId, String paymentId, PaymentWebhookPaymentStatus eventStatus) {
        this.webhookId = webhookId;
        this.paymentId = paymentId;
        this.eventStatus = eventStatus;
        this.status = WebhookStatus.RECEIVED;
        this.receivedAt = LocalDateTime.now();
    }
    public void completeProcess() {
        this.status = WebhookStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }
}
