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
@Table(name="users")
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

    private LocalDateTime deletedAt;

    @Builder
    public User(String username, String phone, String email,
                String password, BigDecimal pointBalance) {

        this.username = username;

        this.phone = phone;

        this.email = email;

        this.password = password;

        //가입 시 관리자 임의로 포인트 지급 가능(가입 이벤트 등)
        this.pointBalance = pointBalance;

        // 생성 시 기본 권한은 USER
        this.role = UserRole.USER;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void addPoints(BigDecimal earnedPoints) {
        this.pointBalance = this.pointBalance.add(earnedPoints);
    }

    public void expirePoints(BigDecimal deductedPoints) {
        if (this.pointBalance.compareTo(deductedPoints) < 0) {
            // 만료 처리는 강제 실행이므로 0으로 설정
            this.pointBalance = BigDecimal.ZERO;
        }
         else {
            this.pointBalance = this.pointBalance.subtract(deductedPoints);
        }
    }

}
