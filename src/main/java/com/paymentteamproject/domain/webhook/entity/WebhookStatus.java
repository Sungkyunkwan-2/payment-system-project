package com.paymentteamproject.domain.webhook.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebhookStatus {
    RECEIVED("요청 받음"),
    PROCESSED("요청 처리완료"),
    FAILED("처리 실패");

    private final String description;
}
