package com.paymentteamproject.domain.pointTransaction.repository;


import com.paymentteamproject.domain.order.entity.Orders;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransaction;
import com.paymentteamproject.domain.pointTransaction.entity.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    //특정 주문으로 적립된 포인트 조회
    @Query("SELECT pt.points FROM PointTransaction pt " +
            "WHERE pt.order.id = :orderId " +
            "AND pt.type = :type")
    Optional<BigDecimal> findPointsByOrderIdAndType(
            @Param("orderId") Long orderId,
            @Param("type") PointTransactionType type
    );


    //특정 주문의 PENDING(적립 예정) 포인트 조회
    default BigDecimal findEarnedPointsByOrderId(Long orderId) {
        return findPointsByOrderIdAndType(orderId, PointTransactionType.PENDING)
                .orElse(BigDecimal.valueOf(0.0));
    }


    //만료 대상 포인트 조회 (ADDED 타입이면서 만료 시간이 지난 것)
    @Query("SELECT pt FROM PointTransaction pt " +
            "WHERE pt.type = 'ADDED' " +
            "AND pt.validity = true " +
            "AND pt.expiresAt < :now " +
            "AND pt.deletedAt IS NULL")
    List<PointTransaction> findExpiredPoints(@Param("now") LocalDateTime now);

    Optional<PointTransaction> findByOrderAndType(Orders order, PointTransactionType type);

    Optional<PointTransaction> findByOrderAndTypeAndValidityTrue(
            Orders order,
            PointTransactionType type
    );

    //만료 시간이 지난 PENDING 트랜잭션 조회
    List<PointTransaction> findByTypeAndValidityTrueAndExpiresAtBefore(PointTransactionType type, LocalDateTime dateTime);
}
