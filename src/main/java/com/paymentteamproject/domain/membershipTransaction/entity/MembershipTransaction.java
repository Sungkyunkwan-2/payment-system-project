package com.paymentteamproject.domain.membershipTransaction.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.masterMembership.entity.MasterMembership;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "membership_transactions")
@NoArgsConstructor
public class MembershipTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // master_memberships(membership) FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "membership",
            referencedColumnName = "membership",
            nullable = false
    )
    private MasterMembership masterMembership;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MembershipTransaction(User user, MasterMembership masterMembership) {
        this.user = user;
        this.masterMembership = masterMembership;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}