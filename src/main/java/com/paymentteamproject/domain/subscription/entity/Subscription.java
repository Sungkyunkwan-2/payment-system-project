package com.paymentteamproject.domain.subscription.entity;

import com.paymentteamproject.domain.paymentmethod.entity.PaymentMethod;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
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
public class Subscription {
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
}
