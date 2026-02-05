package com.paymentteamproject.domain.masterMembership.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "master_memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterMembership extends BaseEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "membership", nullable = false, length = 30)
    private MembershipStatus membership;

    @Column(name = "ratio", nullable = false)
    private double ratio;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MasterMembership(MembershipStatus membership, double ratio) {
        this.membership = membership;
        this.ratio = ratio;
    }

    public void changeRatio(double ratio) {
        this.ratio = ratio;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}