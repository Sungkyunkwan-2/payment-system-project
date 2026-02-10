package com.paymentteamproject.domain.order.controller;

import com.paymentteamproject.common.dto.ApiResponse;
import com.paymentteamproject.domain.order.dto.CreateOrderRequest;
import com.paymentteamproject.domain.order.dto.CreateOrderResponse;
import com.paymentteamproject.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CreateOrderRequest request
    ){
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        HttpStatus.CREATED, "주문 생성에 성공했습니다.", orderService.createOrder(user.getUsername(), request)
                )
        );
    }
}
