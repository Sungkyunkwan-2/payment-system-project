package com.paymentteamproject.domain.payment.service;

import com.paymentteamproject.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PaymentServiceTest {
    @Test
    @DisplayName("금액 합산 테스트: 기존 금액에 새로운 금액이 정확히 더해져야 한다.")
    void updateTotalSpend_success() {
        // given
        User user = User.builder().build();
        BigDecimal delta = new BigDecimal("5000");

        // when
        user.updateTotalSpend(delta);

        // then
        assertThat(user.getTotalSpend()).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("Null 방어 테스트: delta가 null이면 금액에 변화가 없어야 한다.")
    void updateTotalSpend_null_safe() {
        // given
        User user = User.builder().build();

        // when
        user.updateTotalSpend(null);

        // then
        assertThat(user.getTotalSpend()).isEqualTo(BigDecimal.ZERO);
    }
}