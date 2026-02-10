package com.paymentteamproject.domain.pointTransaction.repository;


import com.paymentteamproject.domain.pointTransaction.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    //특정 주문으로 적립된 포인트 조회
    @Query("SELECT pt.points FROM PointTransaction pt " +
            "WHERE pt.order.id = :orderId " +
            "AND pt.type = :type")
    Optional<Double> findPointsByOrderIdAndType(
            @Param("orderId") Long orderId,
            @Param("type") PointTransactionType type
    );


    //특정 주문의 ADDED(적립) 포인트 조회
    default Double findEarnedPointsByOrderId(Long orderId) {
        return findPointsByOrderIdAndType(orderId, PointTransactionType.ADDED)
                .orElse(0.0);
    }
}
