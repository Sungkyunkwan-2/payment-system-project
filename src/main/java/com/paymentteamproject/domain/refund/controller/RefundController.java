package com.paymentteamproject.domain.refund.controller;

import com.paymentteamproject.common.dto.ApiResponse;
import com.paymentteamproject.domain.refund.dto.RefundCreateRequest;
import com.paymentteamproject.domain.refund.dto.RefundCreateResponse;
import com.paymentteamproject.domain.refund.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/refunds/{paymentId}")
    public ResponseEntity<ApiResponse<RefundCreateResponse>> requestRefund(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable String paymentId,
            @Valid @RequestBody RefundCreateRequest request

    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.", null)
            );
        }

        String email = user.getUsername();

        RefundCreateResponse response = refundService.requestRefund(paymentId, email, request);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "요청 접수", response));
    }
}
