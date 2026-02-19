package com.paymentteamproject.domain.membershipTransaction.repository;

import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MembershipHistoryRepository extends JpaRepository<MembershipHistory, Long> {

    @Query("SELECT mh FROM MembershipHistory mh " +
            "WHERE mh.user.id = :userId " +
            "AND mh.deletedAt IS NULL " +
            "ORDER BY mh.createdAt DESC " +
            "LIMIT 1")
    Optional<MembershipHistory> findByUserId(@Param("userId") Long userId);

    Optional<MembershipHistory> findFirstByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
}