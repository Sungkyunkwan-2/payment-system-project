package com.paymentteamproject.domain.subscription.controller;

import com.paymentteamproject.common.dto.ApiResponse;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionRequest;
import com.paymentteamproject.domain.subscription.dto.CreateSubscriptionResponse;
import com.paymentteamproject.domain.subscription.dto.GetSubscriptionResponse;
import com.paymentteamproject.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    // 구독 신청
    @PostMapping("/subscriptions")
    public ResponseEntity<ApiResponse<CreateSubscriptionResponse>> createSubscription(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody CreateSubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        HttpStatus.OK,
                        "성공적으로 구독을 생성했습니다.",
                        subscriptionService.create(user.getUsername(), request)));
    }

    // 구독 조회
    @GetMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<ApiResponse<GetSubscriptionResponse>> getOneSubscription(
            @PathVariable String subscriptionId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        HttpStatus.OK,
                        "구독 정보를 불러오기를 성공했습니다.",
                        subscriptionService.getOne(subscriptionId)));
    }
}
