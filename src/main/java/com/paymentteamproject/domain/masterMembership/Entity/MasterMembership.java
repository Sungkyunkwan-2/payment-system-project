package com.paymentteamproject.domain.masterMembership.Entity;

import com.paymentteamproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "master_memberships")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterMembership extends BaseEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "membership", nullable = false, length = 30)
    private Membership membership;

    @Column(name = "ratio", nullable = false, precision = 10, scale = 4)
    private BigDecimal ratio;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MasterMembership(Membership membership, BigDecimal ratio) {
        this.membership = membership;
        this.ratio = ratio;
    }

    public void changeRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}