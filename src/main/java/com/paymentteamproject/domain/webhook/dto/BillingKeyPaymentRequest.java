package com.paymentteamproject.domain.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class BillingKeyPaymentRequest {
    private final String storeId;
    private final String billingKey;
    private final String orderName;
    private final String customerId;

    @JsonProperty("amount")
    private final Amount amount;

    private final String currency;
    private final String channelKey;

    public BillingKeyPaymentRequest(String storeId, String billingKey, String orderName,
                                    String customerId, BigDecimal totalAmount,
                                    String currency, String channelKey) {
        this.storeId = storeId;
        this.billingKey = billingKey;
        this.orderName = orderName;
        this.customerId = customerId;
        this.amount = new Amount(totalAmount.longValue());
        this.currency = currency;
        this.channelKey = channelKey;
    }

    public BigDecimal getTotalAmount() {
        return BigDecimal.valueOf(amount.getTotal());
    }

    @Getter
    public static class Amount {
        @JsonProperty("total")
        private final Long total;

        @JsonProperty("taxFree")
        private final Long taxFree;

        @JsonProperty("vat")
        private final Long vat;

        public Amount(Long total) {
            this.total = total;
            this.taxFree = null;
            this.vat = null;
        }
    }
}