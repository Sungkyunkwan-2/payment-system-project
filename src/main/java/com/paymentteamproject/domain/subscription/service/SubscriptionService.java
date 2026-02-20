package com.paymentteamproject.domain.subscription.service;

import com.paymentteamproject.config.PortOneProperties;
import com.paymentteamproject.domain.billing.consts.BillingStatus;
import com.paymentteamproject.domain.billing.entity.Billing;
import com.paymentteamproject.domain.billing.repository.BillingRepository;
import com.paymentteamproject.domain.paymentMethod.consts.PaymentMethodStatus;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.paymentMethod.repository.PaymentMethodRepository;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.plan.exception.PlanNotFoundException;
import com.paymentteamproject.domain.plan.repository.PlanRepository;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.subscription.dto.*;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.subscription.exception.SubscriptionNotFoundException;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentRequest;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentResponse;
import com.paymentteamproject.domain.webhook.service.PortOneClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final BillingRepository billingRepository;
    private final PortOneClient portOneClient;
    private final PortOneProperties portOneProperties;

    // 구독 신청
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
                SubscriptionStatus.ACTIVE);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        return new CreateSubscriptionResponse(savedSubscription.getSubscriptionId());
    }

    // 구독 조회
    @Transactional(readOnly = true)
    public GetSubscriptionResponse getOne(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId).orElseThrow(
                () -> new SubscriptionNotFoundException("구독 ID와 일치하는 구독이 없습니다."));

        return new GetSubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getPaymentMethod().getCustomerUid(),
                subscription.getPlan().getPlanId(),
                subscription.getPaymentMethod().getPaymentMethodId(),
                subscription.getStatus(),
                subscription.getPlan().getPrice(),
                subscription.getCurrentPeriodEnd());
    }

    // 구독 해지
    @Transactional
    public UpdateSubscriptionResponse update(String subscriptionId, UpdateSubscriptionRequest request) {
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId).orElseThrow(
                () -> new SubscriptionNotFoundException("구독 ID와 일치하는 구독이 없습니다."));

        subscription.cancel(request.getReason());

        return new UpdateSubscriptionResponse(
                subscription.getSubscriptionId(),
                subscription.getStatus());
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void processRecurringPayments() {
        log.info("스케줄러 시작");
        LocalDateTime now = LocalDateTime.now();
        try {
            // 결제 대상 구독 조회
            List<Subscription> dueSubscriptions = subscriptionRepository.findByCurrentPeriodEndBefore(now);
            if (dueSubscriptions.isEmpty()) {
                log.info("결제 대상 구독이 없습니다.");
                return;
            }

            // 각 구독에 대해 결제 처리
            int successCount = 0;
            int failureCount = 0;

            for (Subscription subscription : dueSubscriptions) {
                try {
                    boolean success = processSubscriptionPayment(subscription);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    log.error("구독 결제 처리 중 예외 발생 - subscriptionId: {}",
                            subscription.getId(), e);
                    failureCount++;
                }
            }

            log.info("정기 결제 스케줄러 완료 - 성공: {}, 실패: {}",
                    successCount, failureCount);

        } catch (Exception e) {
            log.error("정기 결제 스케줄러 실행 중 예외 발생", e);
        }
    }

    @Transactional
    public boolean processSubscriptionPayment(Subscription subscription) {
        Long subscriptionId = subscription.getId();

        LocalDateTime periodStart = subscription.getCurrentPeriodStart();
        LocalDateTime periodEnd = subscription.getCurrentPeriodEnd();

        try {
            // 중복 결제 방지
            if (billingRepository.existsBySubscriptionIdAndPeriodStartAndPeriodEnd(
                    subscriptionId, periodStart, periodEnd)) {

                log.warn("이미 청구된 기간입니다 - subscriptionId: {}, period: {} ~ {}",
                        subscriptionId, periodStart, periodEnd);
                return false;
            }

            PaymentMethod paymentMethod = subscription.getPaymentMethod();
            String billingKey = paymentMethod.getBillingKey();

            if (billingKey == null || billingKey.isEmpty()) {
                log.error("빌링키가 없습니다 - subscriptionId: {}", subscriptionId);
                createFailedBilling(subscription, periodStart, periodEnd, "빌링키가 없습니다");
                return false;
            }

            if (paymentMethod.getStatus() != PaymentMethodStatus.ACTIVE) {
                log.error("결제 수단이 활성화 상태가 아닙니다 - subscriptionId: {}, status: {}",
                        subscriptionId, paymentMethod.getStatus());
                createFailedBilling(subscription, periodStart, periodEnd, "결제 수단 비활성");
                return false;
            }

            Plan plan = subscription.getPlan();
            BigDecimal amount = plan.getPrice();

            BillingKeyPaymentRequest paymentRequest = new BillingKeyPaymentRequest(
                    portOneProperties.getStore().getId(),
                    billingKey,
                    plan.getName() + " 정기결제",
                    paymentMethod.getCustomerUid(),
                    amount,
                    "KRW",
                    portOneProperties.getChannel().get("toss")
            );

            String paymentId =
                    "PAY_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);

            BillingKeyPaymentResponse paymentResponse =
                    portOneClient.payWithBillingKey(paymentRequest, paymentId);

            // 실패처리
            if (paymentResponse.getPayment().getPaidAt() == null) {
                subscription.markAsPastDue();
                return false;
            }

            // 성공 처리
            billingRepository.save(new Billing(
                    subscription,
                    amount,
                    BillingStatus.COMPLETE,
                    paymentId,
                    periodStart,
                    periodEnd,
                    null
            ));

            subscription.renewPeriod();
            return true;

        } catch (Exception e) {
            log.error("구독 결제 실패 - subscriptionId: {}", subscriptionId, e);

            createFailedBilling(subscription, periodStart, periodEnd,
                    "시스템 오류: " + e.getMessage());

            subscription.markAsPastDue();
            return false;
        }
    }


    private void createFailedBilling(Subscription subscription,
                                     LocalDateTime periodStart, LocalDateTime periodEnd, String failureMessage) {
        Billing billing = new Billing(
                subscription,
                subscription.getPlan().getPrice(),
                BillingStatus.FAILED,
                "FAILED_" + UUID.randomUUID(),
                periodStart,
                periodEnd,
                failureMessage
        );
        billingRepository.save(billing);
    }
}
