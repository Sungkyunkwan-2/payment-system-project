package com.paymentteamproject.domain.subscription.repository;

import com.paymentteamproject.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findDueSubscriptionsForPayment(LocalDateTime now);
    Optional<Subscription> findBySubscriptionId(String subscriptionId);
}
