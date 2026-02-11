package com.paymentteamproject.domain.subscription.service;

import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionRequest;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionResponse;
import com.paymentteamproject.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public CreateSubscriptionResponse save(CreateSubscriptionRequest request) {

    }
}
