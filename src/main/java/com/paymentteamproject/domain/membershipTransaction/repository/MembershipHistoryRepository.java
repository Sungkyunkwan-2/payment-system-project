package com.paymentteamproject.domain.membershipTransaction.repository;

import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Long> {

    /**
     * 사용자의 활성 멤버십 조회
     * - deletedAt이 null인 것만 (삭제되지 않은)
     * - 가장 최근에 생성된 멤버십
     */
    @Query("SELECT mh FROM MembershipHistory mh " +
            "WHERE mh.user.id = :userId " +
            "AND mh.deletedAt IS NULL " +
            "ORDER BY mh.createdAt DESC " +
            "LIMIT 1")
    Optional<MembershipHistory> findByUserId(@Param("userId") Long userId);
}