package com.paymentteamproject.domain.webhook.dto;

import lombok.Data;
import lombok.Getter;

/**
 * PortOne 빌링키 결제 응답 DTO
 */
@Getter
public class BillingKeyPaymentResponse {
    private String paymentId;           // 결제 고유 ID
    private String status;              // 결제 상태 (PAID, FAILED 등)
    private String failureReason;       // 실패 사유
    private PaymentAmount amount;       // 결제 금액 정보
    private String paidAt;              // 결제 완료 시각

    @Data
    public static class PaymentAmount {
        private int total;              // 총 결제액
        private int taxFree;            // 면세액
        private int vat;                // 부가세
    }
}