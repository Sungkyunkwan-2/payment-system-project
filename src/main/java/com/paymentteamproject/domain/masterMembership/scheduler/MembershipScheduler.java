package com.paymentteamproject.domain.masterMembership.scheduler;

import com.paymentteamproject.domain.masterMembership.service.MembershipService;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipScheduler {

    private final UserRepository userRepository;
    private final MembershipService membershipService;

    // 매일 새벽 00:00
    @Scheduled(cron = "0 0 0 * * *")
    public void refreshAllMembership() {
        List<Long> userIds = userRepository.findAllActiveUserIds();

        for (Long userId : userIds) {
            try {
                membershipService.refreshMembership(userId);
            } catch (Exception e) {
                log.warn("Membership refresh failed. userId={}", userId, e);
            }
        }
    }
}