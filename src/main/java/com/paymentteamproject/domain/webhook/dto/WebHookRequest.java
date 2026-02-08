package com.paymentteamproject.domain.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebHookRequest {

    @NotBlank(message = "type은 필수 값입니다.")
    @JsonProperty("type")
    private String type;

    @NotBlank(message = "timestamp는 필수 값입니다.")
    @JsonProperty("timestamp")
    private String timestamp;

    @NotNull(message = "data는 필수 값입니다")
    @JsonProperty("data")
    private WebhookData data;

    @Getter
    @NoArgsConstructor
    public static class WebhookData {

        @NotBlank(message = "transactionId는 필수 값입니다.")
        @JsonProperty("transactionId")
        private String transactionId;

        @NotBlank(message = "paymentId는 필수 값입니다.")
        @JsonProperty("paymentId")
        private String paymentId;

        @NotBlank(message = "storeId는 필수 값입니다.")
        @JsonProperty("storeId")
        private String storeId;

        @NotNull(message = "status는 필수 값입니다")
        @JsonProperty("status")
        private PaymentWebhookPaymentStatus status;
    }
}