package com.paymentteamproject.domain.webhook.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PortOne 빌링키 결제 요청 DTO
 */
@Getter
@RequiredArgsConstructor
public class BillingKeyPaymentRequest {
    private final String storeId;           // 상점 ID
    private final String billingKey;        // 빌링키
    private final String orderName;         // 주문명
    private final String customerId;        // 고객 ID
    private final int totalAmount;          // 결제 금액
    private final String currency;          // 통화 (KRW)
    private final String channelKey;        // 채널 키
}