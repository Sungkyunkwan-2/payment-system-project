package com.paymentteamproject.domain.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WebHookRequest {
    @JsonProperty("type")
    private String type;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("data")
    private Data data;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("paymentId")
        private String paymentId;

        @JsonProperty("storeId")
        private String storeId;
    }
}