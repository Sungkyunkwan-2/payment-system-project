package com.paymentteamproject.domain.billing.service;

import com.paymentteamproject.domain.billing.consts.BillingStatus;
import com.paymentteamproject.domain.billing.dto.CreateBillingRequest;
import com.paymentteamproject.domain.billing.dto.CreateBillingResponse;
import com.paymentteamproject.domain.billing.dto.GetBillingResponse;
import com.paymentteamproject.domain.billing.entity.Billing;
import com.paymentteamproject.domain.billing.repository.BillingRepository;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.subscription.exception.SubscriptionNotFoundException;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingService {
    private final BillingRepository billingRepository;
    private final SubscriptionRepository subscriptionRepository;

    // 즉시 결제
    @Transactional
    public CreateBillingResponse create(String subscriptionId, CreateBillingRequest request) {
        Subscription subscription = subscriptionRepository.findBySubscriptionId(subscriptionId).orElseThrow(
                () -> new SubscriptionNotFoundException("구독 ID와 일치하는 구독이 없습니다."));

        // TODO 구독 상태 조건에 따른 예외 처리
        // TODO portOne API 값 조회해서 paymentId 조회랑 결제 성공 실패 여부 판단
        // TODO 싹 다 갈아 엎어야 할듯합니다 -> 정기 결제를 테스트하는 용도로 로직을 짜야합니다.

        Billing billing = new Billing(
                subscription,
                subscription.getPlan().getPrice(),
                BillingStatus.COMPLETE,
                "PAY111770788224392",
                request.getPeriodStart(),
                request.getPeriodEnd()
                );

        Billing savedBilling = billingRepository.save(billing);

        // TODO paymentId required false고, paymentId 불러오는 로직 미구현
        return new CreateBillingResponse(
                savedBilling.getBillingId(),
                savedBilling.getPaymentId(),
                savedBilling.getAmount(),
                savedBilling.getStatus());
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
}
