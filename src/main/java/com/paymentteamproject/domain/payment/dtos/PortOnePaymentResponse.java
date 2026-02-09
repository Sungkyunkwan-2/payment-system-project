package com.paymentteamproject.domain.payment.dtos;

import lombok.Data;
import lombok.Getter;

@Getter
public class PortOnePaymentResponse {
    private String id;          // paymentId
    private String status;      // READY, PAID, CANCELLED, FAILED
    private PaymentAmount amount;
    private String requestedAt;
    private String paidAt;

    @Data
    public static class PaymentAmount {
        private int total;      // 고객이 실제 결제한 총 금액
        private int taxFree;    // 면세액
        private int vat;        // 부가세
        private int supply;     // 공급가액
        private int discount;   // 할인액
    }
}
