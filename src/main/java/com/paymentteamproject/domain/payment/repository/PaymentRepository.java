package com.paymentteamproject.domain.payment.repository;

import com.paymentteamproject.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
