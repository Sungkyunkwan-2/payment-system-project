package com.paymentteamproject.domain.pointTransactions.repository;


import com.paymentteamproject.domain.pointTransactions.entity.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
}
