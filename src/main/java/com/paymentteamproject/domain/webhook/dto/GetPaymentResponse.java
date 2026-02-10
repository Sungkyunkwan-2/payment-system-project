package com.paymentteamproject.domain.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paymentteamproject.domain.webhook.entity.PaymentWebhookPaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetPaymentResponse {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("storeId")
    private String storeId;
    
    @JsonProperty("status")
    private PaymentWebhookPaymentStatus status;
    
    @JsonProperty("orderName")
    private String orderName;
    
    @JsonProperty("amount")
    private Amount amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("paidAt")
    private String paidAt;
    
    @Getter
    @NoArgsConstructor
    public static class Amount {
        @JsonProperty("total")
        private Long total;
        
        @JsonProperty("taxFree")
        private Long taxFree;
        
        @JsonProperty("vat")
        private Long vat;
    }
}