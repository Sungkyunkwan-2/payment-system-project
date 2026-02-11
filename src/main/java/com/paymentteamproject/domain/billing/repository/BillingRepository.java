package com.paymentteamproject.domain.billing.repository;

import com.paymentteamproject.domain.billing.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BillingRepository extends JpaRepository<Billing, Long> {
    boolean existsBySubscriptionIdAndPeriodStartAndPeriodEnd(Long subscriptionId, LocalDateTime periodStart, LocalDateTime periodEnd);
}
