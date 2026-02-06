package com.paymentteamproject.domain.webhook.dto;

import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebHookRequest {

    @NotBlank(message = "type은 필수 값입니다.")
    private String type;

    @NotBlank(message = "timestamp는 필수 값입니다.")
    private String timestamp;

    @NotNull(message = "data는 필수 값입니다")
    private WebhookData data;

    @Getter
    @NoArgsConstructor
    public static class WebhookData {

        @NotBlank(message = "transactionId는 필수 값입니다.")
        private String transactionId;

        @NotBlank(message = "paymentId는 필수 값입니다.")
        private String paymentId;

        @NotBlank(message = "storeId는 필수 값입니다.")
        private String storeId;

        @NotBlank(message = "status는 필수 값입니다")
        private String status; // "PAID", "CANCELLED", "FAILED" 등 - String으로 받아서 나중에 Enum 변환
    }
}
