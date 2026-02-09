package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.config.PortOneProperties;
import com.paymentteamproject.domain.webhook.dto.GetPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final RestClient portOneRestClient;
    private final PortOneProperties portOneProperties;

    public GetPaymentResponse getPayment(String paymentId) {
        return portOneRestClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + portOneProperties.getApi().getSecret())
                .retrieve()
                .body(GetPaymentResponse.class);
    }
}
