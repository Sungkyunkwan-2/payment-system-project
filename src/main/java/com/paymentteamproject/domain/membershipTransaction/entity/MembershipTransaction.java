package com.paymentteamproject.domain.membershipTransaction.entity;

import com.paymentteamproject.common.entity.BaseEntity;
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


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MembershipTransaction(User user) {
        this.user = user;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}