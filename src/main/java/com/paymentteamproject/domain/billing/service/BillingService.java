package com.paymentteamproject.domain.billing.service;

import com.paymentteamproject.config.PortOneProperties;
import com.paymentteamproject.domain.billing.consts.BillingStatus;
import com.paymentteamproject.domain.billing.dto.CreateBillingRequest;
import com.paymentteamproject.domain.billing.dto.CreateBillingResponse;
import com.paymentteamproject.domain.billing.dto.GetBillingResponse;
import com.paymentteamproject.domain.billing.entity.Billing;
import com.paymentteamproject.domain.subscription.exception.InactiveSubscriptionException;
import com.paymentteamproject.domain.billing.repository.BillingRepository;
import com.paymentteamproject.domain.paymentMethod.consts.PaymentMethodStatus;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.subscription.exception.SubscriptionAlreadyBilledException;
import com.paymentteamproject.domain.subscription.exception.SubscriptionNotFoundException;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentRequest;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentResponse;
import com.paymentteamproject.domain.webhook.service.PortOneClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {
    private final BillingRepository billingRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PortOneProperties portOneProperties;
    private final PortOneClient portOneClient;

    // 즉시 결제
    @Transactional
    public CreateBillingResponse create(String subscriptionId, CreateBillingRequest request) {
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId).orElseThrow(
                () -> new SubscriptionNotFoundException("구독 ID와 일치하는 구독이 없습니다."));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new InactiveSubscriptionException("활성된 구독만 결제할 수 있습니다.");
        }

        boolean alreadyBilled = billingRepository.existsBySubscriptionIdAndPeriodStartAndPeriodEnd(
                subscription.getId(), subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
        if(alreadyBilled) {
            throw new SubscriptionAlreadyBilledException(
                    subscription.getCurrentPeriodStart() + " ~ " + subscription.getCurrentPeriodEnd() + " 기간 내 청구된 구독입니다.");
        }

        //빌링키 검증
        PaymentMethod paymentMethod = subscription.getPaymentMethod();
        String billingKey = paymentMethod.getBillingKey();

        if (billingKey == null || billingKey.isEmpty()) {
            return createFailedBillingResponse(subscription, request,"빌링키가 없습니다.");
        }

        if (paymentMethod.getStatus() != PaymentMethodStatus.ACTIVE) {
            return createFailedBillingResponse(subscription, request, "결제 수단이 활성 이 아닙니다.");
        }

        //결제 실행
        Plan plan = subscription.getPlan();
        BigDecimal amount = plan.getPrice();

        try{
            BillingKeyPaymentRequest paymentRequest = new BillingKeyPaymentRequest(
                    portOneProperties.getStore().getId(),
                    billingKey,
                    plan.getName() + " 즉시결제",
                    paymentMethod.getCustomerUid(),
                    amount,
                    "KRW",
                    portOneProperties.getChannel().get("toss")
            );

            String paymentId = "PAY_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);

            BillingKeyPaymentResponse paymentResponse = portOneClient.payWithBillingKey(paymentRequest, paymentId);

            // 5. 결제 결과에 따른 처리
            if (paymentResponse.getPayment().getPaidAt() != null) {
                subscription.renewPeriod();

                Billing billing = new Billing(
                        subscription,
                        amount,
                        BillingStatus.COMPLETE,
                        paymentId,
                        subscription.getCurrentPeriodStart(),
                        subscription.getCurrentPeriodEnd(),
                        null
                );

                Billing savedBilling = billingRepository.save(billing);

                return new CreateBillingResponse(
                        savedBilling.getBillingId(),
                        savedBilling.getPaymentId(),
                        savedBilling.getAmount(),
                        savedBilling.getStatus());

            } else {
                return createFailedBillingResponse(subscription, request, "알 수 없는 오류");
            }

        } catch (Exception e) {
            return createFailedBillingResponse(subscription, request,
                    "시스템 오류");
        }
    }

    // 청구 내역 조회
    @Transactional(readOnly = true)
    public List<GetBillingResponse> getAll(String subscriptionId) {
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId).orElseThrow(
                () -> new SubscriptionNotFoundException("구독 ID와 일치하는 구독이 없습니다."));

        List<Billing> billings = billingRepository.findBySubscription(subscription);

        return billings.stream().map(b -> new GetBillingResponse(
                b.getBillingId(),
                b.getPeriodStart(),
                b.getPeriodEnd(),
                b.getAmount(),
                b.getStatus(),
                b.getPaymentId(),
                b.getAttemptedAt(),
                b.getFailureMessage()))
                .toList();
    }

    private CreateBillingResponse createFailedBillingResponse(
            Subscription subscription, CreateBillingRequest request, String failureMessage) {

        subscription.renewPeriod();

        Billing billing = new Billing(
                subscription,
                subscription.getPlan().getPrice(),
                BillingStatus.FAILED,
                "PAY_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                failureMessage
        );

        Billing savedBilling = billingRepository.save(billing);

        return new CreateBillingResponse(
                savedBilling.getBillingId(),
                savedBilling.getPaymentId(),
                savedBilling.getAmount(),
                savedBilling.getStatus());
    }
}
