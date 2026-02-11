package com.paymentteamproject.domain.user.service;

import com.paymentteamproject.domain.payment.event.TotalSpendChangedEvent;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Slf4j
@Component // 더 가벼움
@RequiredArgsConstructor
public class UserEventListener {
    private final UserRepository userRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTotalSpendChangedEvent(TotalSpendChangedEvent event){
        Long userId = event.user().getId();
        BigDecimal delta = event.delta() == null ? BigDecimal.ZERO : event.delta();

        log.info("이벤트 수신(AFTER_COMMIT): userId={}, delta={}", userId, delta);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateTotalSpend(delta);

        log.info("업데이트 완료: userId={}, totalSpend={}", userId, user.getTotalSpend());
        }
}
