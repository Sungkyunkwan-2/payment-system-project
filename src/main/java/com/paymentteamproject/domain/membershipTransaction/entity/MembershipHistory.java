package com.paymentteamproject.domain.membershipTransaction.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.membershipTransaction.consts.MembershipStatus;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "membership_transactions")
@NoArgsConstructor
public class MembershipHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    @Enumerated(EnumType.STRING)
    private MembershipStatus membershipStatus;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MembershipHistory(User user, MembershipStatus membershipStatus) {
        this.user = user;
        this.membershipStatus = membershipStatus;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}