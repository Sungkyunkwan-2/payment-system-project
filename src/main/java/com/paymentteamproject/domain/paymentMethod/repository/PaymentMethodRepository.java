package com.paymentteamproject.domain.paymentMethod.repository;

import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}
