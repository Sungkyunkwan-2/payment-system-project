package com.paymentteamproject.domain.subscription.repository;

import com.paymentteamproject.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
