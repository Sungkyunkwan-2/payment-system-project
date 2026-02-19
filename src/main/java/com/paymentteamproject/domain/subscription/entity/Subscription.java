package com.paymentteamproject.domain.subscription.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.subscription.exception.InvalidCancelSubscriptionException;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false)
    private String subscriptionId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private String canceledReason;

    private LocalDateTime canceledAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime currentPeriodStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime currentPeriodEnd;

    public Subscription(User user, Plan plan, PaymentMethod paymentMethod, SubscriptionStatus status) {
        this.user = user;
        this.plan = plan;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    @PrePersist
    private void generateSubscriptionId() {
        if (this.subscriptionId == null) {
            this.subscriptionId = "SUB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }

    public void cancel(String canceledReason) {
        if (this.status != SubscriptionStatus.ACTIVE) {
            throw new InvalidCancelSubscriptionException("활성화된 구독만 취소 가능합니다.");
        }

        this.canceledReason = canceledReason;
        this.canceledAt = LocalDateTime.now();
        this.status = SubscriptionStatus.CANCELLED;
    }


    public void markAsPastDue() {
        this.status = SubscriptionStatus.UNPAID;
    }

    public void renewPeriod() {
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = this.plan.getBillingCycle().calculatePeriodEnd(this.currentPeriodStart);
    }
}
