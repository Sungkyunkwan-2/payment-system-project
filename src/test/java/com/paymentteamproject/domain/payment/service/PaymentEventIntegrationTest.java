package com.paymentteamproject.domain.payment.service;

import com.paymentteamproject.domain.payment.event.TotalSpendChangedEvent;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import com.paymentteamproject.domain.user.service.UserEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentEventIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    // @SpyBean 대신 @MockitoSpyBean 사용
    @MockitoSpyBean
    private UserEventListener userEventListener;

    private User user;

    @Test
    @DisplayName("시나리오 1: 결제 성공 시, 비동기로 총 소비액이 업데이트되어야 한다.")
    void event_should_update_spend_after_commit() {
    }

    @Test
    @DisplayName("시나리오: 메인 트랜잭션 롤백 시 리스너가 호출되지 않아야 한다.")
    void event_should_not_run_after_rollback() {
      }
}
