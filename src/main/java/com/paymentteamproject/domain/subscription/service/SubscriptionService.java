package com.paymentteamproject.domain.subscription.service;

import com.paymentteamproject.domain.paymentMethod.consts.PaymentMethodStatus;
import com.paymentteamproject.domain.paymentMethod.entity.PaymentMethod;
import com.paymentteamproject.domain.paymentMethod.repository.PaymentMethodRepository;
import com.paymentteamproject.domain.plan.entity.Plan;
import com.paymentteamproject.domain.plan.exception.PlanNotFoundException;
import com.paymentteamproject.domain.plan.repository.PlanRepository;
import com.paymentteamproject.domain.subscription.consts.SubscriptionStatus;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionRequest;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionResponse;
import com.paymentteamproject.domain.subscription.entity.Subscription;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;

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
}
