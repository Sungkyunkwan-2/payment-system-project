package com.paymentteamproject.domain.refund.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortOneCancelResponse {

    private String paymentId;
    private String status;
}