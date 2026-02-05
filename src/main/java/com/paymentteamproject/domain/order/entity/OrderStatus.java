package com.paymentteamproject.domain.order.entity;

public enum OrderStatus {
    PAYMENT_PENDING("PAYMENT_PENDING", "결제 대기"),
    ORDER_COMPLETED("ORDER_COMPLETED", "주문 완료"),
    ORDER_CANCELED("ORDER_CANCELED", "주문 취소");

    private final String OrderCode;
    private final String OrderDescription;

    OrderStatus(String orderCode, String orderDescription) {
        OrderCode = orderCode;
        OrderDescription = orderDescription;
    }
}
