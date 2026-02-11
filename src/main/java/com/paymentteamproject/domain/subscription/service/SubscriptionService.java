package com.paymentteamproject.domain.subscription.service;

import com.paymentteamproject.domain.paymentMethod.consts.PaymentMethodStatus;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.paymentMethod.repository.PaymentMethodRepository;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.plan.exception.PlanNotFoundException;
import com.paymentteamproject.domain.plan.repository.PlanRepository;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.billing.consts.BillingStatus;
import com.paymentteamproject.domain.billing.entity.Billing;
import com.paymentteamproject.domain.billing.repository.BillingRepository;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionRequest;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionResponse;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponseException;

import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final BillingRepository billingRepository;

    @Transactional
    public CreateSubscriptionResponse create(String email, CreateSubscriptionRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Plan plan = planRepository.findByPlanId(request.getPlanId()).orElseThrow(
                () -> new PlanNotFoundException("존재하지 않는 구독 플랜입니다."));

        PaymentMethod paymentMethod = new PaymentMethod(
                user,
                request.getBillingKey(),
                request.getCustomerUid(),
                PaymentMethodStatus.ACTIVE);

        PaymentMethod savedPaymentMethod = paymentMethodRepository.save(paymentMethod);

        Subscription subscription = new Subscription(
                user,
                plan,
                savedPaymentMethod,
                SubscriptionStatus.ACTIVE,
                plan.getBillingCycle().calculatePeriodEnd(LocalDateTime.now()));

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return new CreateSubscriptionResponse(savedSubscription.getSubscriptionId());
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
