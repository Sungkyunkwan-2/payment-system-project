package com.paymentteamproject.domain.webhook.dto;

import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebHookRequest {
    @NotBlank(message = "webhookId는 필수 값입니다.")
    private String webhookId;

    @NotBlank(message ="paymentId는 필수 값입니다.")
    private String paymentId;

    @NotNull(message = "eventStatus는 필수 값입니다")
    private PaymentWebhookPaymentStatus eventStatus;
}
