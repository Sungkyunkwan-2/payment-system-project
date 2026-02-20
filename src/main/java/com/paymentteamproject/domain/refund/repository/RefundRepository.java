package com.paymentteamproject.domain.refund.repository;

import com.paymentteamproject.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findTopByPayment_PaymentIdOrderByIdDesc(String paymentId);
}