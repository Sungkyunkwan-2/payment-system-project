package com.paymentteamproject.domain.user.service;

import com.paymentteamproject.domain.payment.event.TotalSpendChangedEvent;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component // 더 가벼움
@RequiredArgsConstructor
public class UserEventListener {
    private final UserRepository userRepository;

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async
    public void handleTotalSpendChangedEvent(TotalSpendChangedEvent event){
        log.info("이벤트 수신: 사용자ID = {}, 추가 금액 = {}", event.user().getId(), event.newSpend());

        User foundUser = userRepository.findById(event.user().getId())
                .orElseThrow(UserNotFoundException::new);

        foundUser.updateTotalSpend(event.newSpend());

        // 명시적으로 DB에 즉시 반영 명령
        userRepository.saveAndFlush(foundUser);

        log.info("업데이트 완료: 총 결제액 = {}", foundUser.getTotalSpend());
    }
}
