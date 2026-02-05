package com.paymentteamproject.domain.user.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String username;

    @Column(nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private double pointBalance;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public User(String username, String phone, String email, String password, double pointBalance){

        this.username = username;

        this.phone = phone;

        this.email = email;

        this.password = password;

        //가입 시 관리자 임의로 포인트 지급 가능(가입 이벤트 등)
        this.pointBalance = pointBalance;
    }

    public void softDelete(){
        this.deletedAt = LocalDateTime.now();
    }
}
