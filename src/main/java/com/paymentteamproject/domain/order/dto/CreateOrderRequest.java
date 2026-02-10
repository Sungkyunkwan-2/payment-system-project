package com.paymentteamproject.domain.order.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    private List<OrderItemRequest> items;
}
