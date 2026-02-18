package com.paymentteamproject.domain.payment.service;

import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUpdateTest {
    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 업데이트 성공: 사용자를 찾아 금액을 업데이트한다.")
    void updateUserSpend_success() {
        // given
        BigDecimal delta = new BigDecimal("5000");
        User user = User.builder().build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        // when
        paymentService.updateUserSpend(999L, delta);

        // then
        assertThat(user.getTotalSpend()).isEqualByComparingTo("5000");
        verify(userRepository, times(1)).findById(999L);
    }
}
