package com.paymentteamproject.domain.refund.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortOneCancelRequest {
    private String reason;

    public PortOneCancelRequest(String reason) {
        this.reason = reason;
    }
}