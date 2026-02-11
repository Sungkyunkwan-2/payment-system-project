package com.paymentteamproject.domain.paymentMethod.entity;

import com.paymentteamproject.common.entity.BaseEntity;
import com.paymentteamproject.domain.paymentMethod.consts.PaymentMethodStatus;
import com.paymentteamproject.domain.paymentMethod.consts.PgProvider;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payment_methods")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String billingKey;

    @Column(nullable = false)
    private String customerUid;

    @Column(nullable = false)
    private PgProvider pgProvider;

    @Column(nullable = false)
    private boolean isDefault;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethodStatus status;

    @Column
    private LocalDateTime deletedAt;

    public PaymentMethod(User user, String billingKey, String customerUid, PgProvider pgProvider, PaymentMethodStatus status) {
        this.user = user;
        this.billingKey = billingKey;
        this.customerUid = customerUid;
        this.pgProvider = pgProvider;
        this.isDefault = false;
        this.status = status;
    }

}
