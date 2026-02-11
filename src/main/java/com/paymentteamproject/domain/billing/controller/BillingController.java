package com.paymentteamproject.domain.billing.controller;

import com.paymentteamproject.common.dto.ApiResponse;
import com.paymentteamproject.domain.billing.dto.CreateBillingRequest;
import com.paymentteamproject.domain.billing.dto.CreateBillingResponse;
import com.paymentteamproject.domain.billing.dto.GetBillingResponse;
import com.paymentteamproject.domain.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BillingController {
    private final BillingService billingService;

    // 즉시 결제
    @PostMapping("/billings/{subscriptionId}")
    public ResponseEntity<ApiResponse<CreateBillingResponse>> createBilling(
            @PathVariable String subscriptionId,
            @RequestBody CreateBillingRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        HttpStatus.OK,
                        "구독 즉시 결제를 성공했습니다.",
                        billingService.create(subscriptionId, request)));
    }

    // 청구 내역 조회
    @GetMapping("/billings/{subscriptionId}")
    public ResponseEntity<ApiResponse<List<GetBillingResponse>>> getAllBilling(
            @PathVariable String subscriptionId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        HttpStatus.OK,
                        "청구 내역 조회를 성공했습니다.",
                        billingService.getAll(subscriptionId)));
    }
}
