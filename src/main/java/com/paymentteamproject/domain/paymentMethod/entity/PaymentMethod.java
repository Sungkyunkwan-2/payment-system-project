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
import java.util.UUID;

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
    private String paymentMethodId;

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

    public PaymentMethod(User user, String billingKey, String customerUid, PaymentMethodStatus status) {
        this.user = user;
        this.billingKey = billingKey;
        this.customerUid = customerUid;
        this.pgProvider = PgProvider.TOSS_PAYMENTS;
        this.isDefault = true;
        this.status = status;
    }

    @PrePersist
    private void generatePaymentMethodId() {
        if (this.paymentMethodId == null) {
            this.paymentMethodId = "PM_CARD_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        }
    }
}
