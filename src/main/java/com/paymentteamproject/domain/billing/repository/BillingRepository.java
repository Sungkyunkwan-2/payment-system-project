package com.paymentteamproject.domain.billing.repository;

import com.paymentteamproject.domain.billing.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing, Long> {
}
