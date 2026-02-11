package com.paymentteamproject.domain.subscription.controller;

import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionRequest;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionResponse;
import com.paymentteamproject.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping("/subscriptions")
    public ResponseEntity<CreateSubscriptionResponse> create(@RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.save(request));
    }
}
