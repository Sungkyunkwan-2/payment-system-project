package com.paymentteamproject.domain.refund.repository;

import com.paymentteamproject.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    // 같은 paymentId에 대한 refund 이벤트들 중 "최신 1건" (멱등성/재시도 판단에 사용)
    Optional<Refund> findTopByPayment_PaymentIdOrderByIdDesc(String paymentId);
}