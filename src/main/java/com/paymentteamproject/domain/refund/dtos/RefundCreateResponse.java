package com.paymentteamproject.domain.refund.dtos;

import com.paymentteamproject.domain.refund.entity.Refund;
import com.paymentteamproject.domain.refund.entity.RefundStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RefundCreateResponse {

    private final Long refundId;
    private final Long paymentId;
    private final double amount;
    private final RefundStatus status;
    private final String reason;
    private final LocalDateTime refundAt;

    public RefundCreateResponse(Long refundId, Long paymentId, double amount, RefundStatus status, String reason, LocalDateTime refundAt) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.status = status;
        this.reason = reason;
        this.refundAt = refundAt;
    }
}