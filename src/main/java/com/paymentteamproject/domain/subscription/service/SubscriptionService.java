package com.paymentteamproject.domain.subscription.service;

import com.paymentteamproject.domain.billing.consts.BillingStatus;
import com.paymentteamproject.domain.billing.entity.Billing;
import com.paymentteamproject.domain.billing.repository.BillingRepository;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionRequest;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionResponse;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final BillingRepository billingRepository;

    @Transactional
    public CreateSubscriptionResponse create(UserDetails user, CreateSubscriptionRequest request) {
       return new CreateSubscriptionResponse("1");
    }









































    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void processRecurringPayments() {
        log.info("===== 정기 결제 스케줄러 시작 =====");
        LocalDateTime now = LocalDateTime.now();

        try {
            List<Subscription> dueSubscriptions = subscriptionRepository.findDueSubscriptionsForPayment(now);

            if (dueSubscriptions.isEmpty()) {
                log.info("결제 대상 구독이 없습니다.");
                return;
            }

            for (Subscription subscription : dueSubscriptions) {
                try {
                    processSubscriptionPayment(subscription);
                } catch (Exception e) {
                    log.error("구독 결제 처리 중 예외 발생 - subscriptionId: {}",
                            subscription.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("정기 결제 스케줄러 실행 중 예외 발생", e);
        }
    }

    @Transactional
    public boolean processSubscriptionPayment(Subscription subscription) {
        Long subscriptionId = subscription.getId();
        log.info("구독 결제 처리 시작 - subscriptionId: {}, userId: {}, planId: {}",
                subscriptionId, subscription.getUser().getId(), subscription.getPlan().getId());

        try {
            // 중복 결제 방지 확인
            LocalDateTime periodStart = subscription.getCreatedAt();
            LocalDateTime periodEnd = subscription.getCurrentPeriodEnd();

            boolean alreadyBilled = billingRepository.existsBySubscriptionIdAndPeriodStartAndPeriodEnd(
                    subscriptionId, periodStart, periodEnd
            );

            if (alreadyBilled) {
                log.warn("이미 청구된 기간: subscriptionId: {}, period: {} ~ {}",
                        subscriptionId, periodStart, periodEnd);
                return false;
            }

            String billingKey = subscription.getPaymentMethod().getBillingKey();
            if (billingKey == null || billingKey.isEmpty()) {
                log.error("빌링키가 없습니다: subscriptionId: {}", subscriptionId);
                return false;
            }
            //ToDO 성공 처리


            return true;

        } catch (Exception e) {
            log.error("구독 결제 실패 - subscriptionId: {}", subscriptionId, e);
            return false;
        }
    }
}
