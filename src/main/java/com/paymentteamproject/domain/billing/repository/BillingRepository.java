package com.paymentteamproject.domain.billing.repository;

import com.paymentteamproject.domain.billing.entity.Billing;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long> {
    List<Billing> findBySubscription(Subscription subscription);
    boolean existsBySubscriptionIdAndPeriodStartAndPeriodEnd(Long subscriptionId, LocalDateTime periodStart, LocalDateTime periodEnd);
}
