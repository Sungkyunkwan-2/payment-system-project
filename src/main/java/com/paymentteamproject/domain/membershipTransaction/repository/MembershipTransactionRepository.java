package com.paymentteamproject.domain.membershipTransaction.repository;

import com.paymentteamproject.domain.membershipTransaction.entity.MembershipTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MembershipTransactionRepository extends JpaRepository<MembershipTransaction, Long> {

    /**
     * 사용자의 활성 멤버십 조회
     * - deletedAt이 null인 것만 (삭제되지 않은)
     * - 가장 최근에 생성된 멤버십
     */
    @Query("SELECT mt FROM MembershipTransaction mt " +
            "JOIN FETCH mt.masterMembership " +
            "WHERE mt.user.id = :userId " +
            "AND mt.deletedAt IS NULL " +
            "ORDER BY mt.createdAt DESC " +
            "LIMIT 1")
    Optional<MembershipTransaction> findByUserId(@Param("userId") Long userId);
}