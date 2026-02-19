package com.paymentteamproject.domain.user.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.user.consts.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private BigDecimal pointBalance;

    @Column(nullable = false)
    private BigDecimal totalSpend;

    private LocalDateTime deletedAt;

    @Builder
    public User(String username, String phone, String email, String password, BigDecimal pointBalance) {
        this.username = username;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.pointBalance = pointBalance;
        this.role = UserRole.USER;
        this.totalSpend = BigDecimal.ZERO;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void addPoints(BigDecimal earnedPoints) {
        this.pointBalance = this.pointBalance.add(earnedPoints);
    }

    public void expirePoints(BigDecimal deductedPoints) {
        if (this.pointBalance.compareTo(deductedPoints) < 0) {
            this.pointBalance = BigDecimal.ZERO;
        } else {
            this.pointBalance = this.pointBalance.subtract(deductedPoints);
        }
    }

    public void subPoints(BigDecimal amount) {
        this.pointBalance = this.pointBalance.subtract(amount);
    }


    public void updateTotalSpend(BigDecimal amount) {
        if (amount == null) {
            return;
        }
        this.totalSpend = this.totalSpend.add(amount);
    }
}
