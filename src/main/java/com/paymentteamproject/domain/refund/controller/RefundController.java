package com.paymentteamproject.domain.refund.controller;

import com.paymentteamproject.common.dtos.ApiResponse;
import com.paymentteamproject.domain.refund.dtos.RefundCreateRequest;
import com.paymentteamproject.domain.refund.dtos.RefundCreateResponse;
import com.paymentteamproject.domain.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/refunds")
    public ApiResponse<RefundCreateResponse> requestRefund(
            @PathVariable Long paymentId,
            @Valid @RequestBody RefundCreateRequest request
            // @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        Long userId = 1L; // 임시

        RefundCreateResponse response = refundService.requestRefund(paymentId, userId, request);

        return ApiResponse.success(HttpStatus.OK, "요청 접수", response);
    }
}