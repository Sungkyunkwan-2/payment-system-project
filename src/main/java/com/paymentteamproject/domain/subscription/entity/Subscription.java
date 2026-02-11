package com.paymentteamproject.domain.subscription.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
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
    @JoinColumn(name = "user_id",  nullable = false)
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

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime currentPeriodEnd;

    private LocalDateTime canceledAt;

    public Subscription(User user, Plan plan, PaymentMethod paymentMethod,
                        SubscriptionStatus status, LocalDateTime currentPeriodEnd)
    {
        this.user = user;
        this.plan = plan;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.currentPeriodEnd = currentPeriodEnd;
    }

    @PrePersist
    private void generateSubscriptionId() {
        if (this.subscriptionId == null) {
            this.subscriptionId = "SUB_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
