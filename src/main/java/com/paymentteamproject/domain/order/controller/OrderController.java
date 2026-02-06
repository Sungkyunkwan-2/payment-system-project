package com.paymentteamproject.domain.order.controller;

import com.paymentteamproject.domain.order.dto.CreateOrderRequest;
import com.paymentteamproject.domain.order.dto.CreateOrderResponse;
import com.paymentteamproject.domain.order.service.OrderService;
import com.paymentteamproject.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateOrderRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(user.getId(), request));
    }
}
