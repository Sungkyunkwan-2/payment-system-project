package com.paymentteamproject.domain.payment.controller;

import com.paymentteamproject.common.dtos.ApiResponse;
import com.paymentteamproject.domain.payment.dtos.ConfirmPaymentResponse;
import com.paymentteamproject.domain.payment.dtos.StartPaymentRequest;
import com.paymentteamproject.domain.payment.dtos.StartPaymentResponse;
import com.paymentteamproject.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    // 결제 시작
    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<StartPaymentResponse>> startPayment(
            @RequestBody StartPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(HttpStatus.CREATED, "결제가 시작되었습니다", paymentService.start(request)));
    }

    // 결제 확인
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<ConfirmPaymentResponse>> confirmPayment(
            @PathVariable String paymentId) {
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK, "성공적으로 결제되었습니다", paymentService.confirm(paymentId)));
    }
}
