package com.paymentteamproject.domain.orderProduct.controller;


import com.paymentteamproject.common.dtos.ApiResponse;
import com.paymentteamproject.domain.orderProduct.dto.getAllOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.dto.getOneOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.service.OrderProductService;
import com.paymentteamproject.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderProductController {
    private final OrderProductService orderProductService;

    //내 주문 목록 조회
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<getAllOrderProductResponse>>> getAllOrderProducts(
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        HttpStatus.OK, "주문 목록 조회에 성공했습니다.", orderProductService.getAllOrderProducts(user.getUsername())
                )
        );
    }

    //내 주문 목록 단건 조회
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<getOneOrderProductResponse>> getOneOrderProducts(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long orderId

    ) {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        HttpStatus.OK, "주문 단건 조회에 성공했습니다.", orderProductService.getOneOrderProducts(user.getUsername(), orderId)
                )
        );
    }
}
