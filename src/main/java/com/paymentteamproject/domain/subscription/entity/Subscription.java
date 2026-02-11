package com.paymentteamproject.domain.subscription.entity;

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
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime currentPeriodStart;
    
    @Column(nullable = false)
    private LocalDateTime currentPeriodEnd;

    private String canceledReason;

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

    public void markAsPastDue() {
        this.status = SubscriptionStatus.UNPAID;
    }

    public void updateStatus(SubscriptionStatus subscriptionStatus) {
        this.status = subscriptionStatus;
    }

    public void renewPeriod() {
        this.currentPeriodStart = this.currentPeriodEnd;
        this.currentPeriodEnd = this.plan.getBillingCycle()
                .calculatePeriodEnd(this.currentPeriodEnd);
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.canceledAt = LocalDateTime.now();
    }

    public void cancel(String canceledReason) {
        if (this.status != SubscriptionStatus.ACTIVE) {
            throw new InvalidCancelSubscriptionException("활성화된 구독만 취소 가능합니다.");
        }

        this.canceledReason = canceledReason;
        this.canceledAt = LocalDateTime.now();
        this.status = SubscriptionStatus.CANCELLED;
    }
}
