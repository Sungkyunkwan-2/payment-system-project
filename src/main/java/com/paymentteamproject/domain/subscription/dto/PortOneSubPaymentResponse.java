package com.paymentteamproject.domain.subscription.dto;

import lombok.Getter;

@Getter
public class PortOneSubPaymentResponse {
    private final String paymentId;
    private final String orderRef;
    private final String merchantOrderRef;
    private final String channelOrderRef;
    private final String status;
    private final String statusCode;
    private final String statusReason;
    private final String statusChannelReason;

    public PortOneSubPaymentResponse(String paymentId, String orderRef, String merchantOrderRef, String channelOrderRef, String status, String statusCode, String statusReason, String statusChannelReason) {
        this.paymentId = paymentId;
        this.orderRef = orderRef;
        this.merchantOrderRef = merchantOrderRef;
        this.channelOrderRef = channelOrderRef;
        this.status = status;
        this.statusCode = statusCode;
        this.statusReason = statusReason;
        this.statusChannelReason = statusChannelReason;
    }

}

