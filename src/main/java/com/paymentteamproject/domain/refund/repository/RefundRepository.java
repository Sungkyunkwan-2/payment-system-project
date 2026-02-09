package com.paymentteamproject.domain.refund.repository;

import com.paymentteamproject.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Refund findByPaymentId(Long paymentId);
}