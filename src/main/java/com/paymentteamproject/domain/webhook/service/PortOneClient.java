package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.config.PortOneProperties;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentRequest;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentResponse;
import com.paymentteamproject.domain.webhook.dto.GetPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final RestClient portOneRestClient;
    private final PortOneProperties portOneProperties;

    public GetPaymentResponse getPayment(String paymentId) {
        log.info("결제 조회 요청 - paymentId: {}", paymentId);

        return portOneRestClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + portOneProperties.getApi().getSecret())
                .retrieve()
                .body(GetPaymentResponse.class);
    }

    public BillingKeyPaymentResponse payWithBillingKey(BillingKeyPaymentRequest request) {
        log.info("빌링키 결제 요청 - billingKey: {}, amount: {}",
                request.getBillingKey(), request.getAmount());

        try {
            BillingKeyPaymentResponse response = portOneRestClient.post()
                    .uri("/payments/{paymentId}/billing-key", request.getBillingKey())
                    .header("Authorization", "PortOne " + portOneProperties.getApi().getSecret())
                    .body(request)
                    .retrieve()
                    .body(BillingKeyPaymentResponse.class);

            return response;

        } catch (Exception e) {
            log.error("빌링키 결제 실패 - billingKey: {}, error: {}",
                    request.getBillingKey(), e.getMessage(), e);
            throw e;
        }
    }
}