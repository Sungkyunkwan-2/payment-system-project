package com.paymentteamproject.domain.orderProduct.controller;


import com.paymentteamproject.domain.orderProduct.dto.getAllOrderProductResponse;
import com.paymentteamproject.domain.orderProduct.service.OrderProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderProductController {
    private final OrderProductService orderProductService;

    //주문 목록 조회
    @GetMapping("/orders")
    public ResponseEntity<List<getAllOrderProductResponse>> getAllOrderProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(orderProductService.getAllOrderProducts());
    }
}
