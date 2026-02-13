package com.paymentteamproject.domain.webhook.service;

import com.paymentteamproject.config.PortOneProperties;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentRequest;
import com.paymentteamproject.domain.webhook.dto.BillingKeyPaymentResponse;
import com.paymentteamproject.domain.webhook.dto.GetPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

    /**
     * 빌링키 결제(청구) 실행
     * - paymentId는 새로 생성해서 path에 넣어야 함
     * - amount는 숫자가 아니라 객체 형태로 보내야 함: { "total": 1000 }
     */
    public BillingKeyPaymentResponse payWithBillingKey(BillingKeyPaymentRequest request) {
        //  paymentId 생성 (중복 방지)
        String paymentId = "PAY_" + UUID.randomUUID();

        log.info("빌링키 결제 요청 - paymentId: {}, billingKey: {}, totalAmount: {}",
                paymentId, request.getBillingKey(), request.getTotalAmount());

        //  PortOne 스펙에 맞는 body 구성
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("storeId", request.getStoreId());
        body.put("billingKey", request.getBillingKey());
        body.put("orderName", request.getOrderName());
        body.put("channelKey", request.getChannelKey());
        body.put("currency", request.getCurrency());

        // customerId만 쓰는게 아니라 customer 객체가 필요할 수 있음 (보수적으로 객체로)
        body.put("customer", Map.of("customerId", request.getCustomerId()));

        //  amount는 객체 형태
        body.put("amount", Map.of("total", request.getTotalAmount()));

        try {
            BillingKeyPaymentResponse response = portOneRestClient.post()
                    .uri("/payments/{paymentId}/billing-key", paymentId) //  여기가 핵심!
                    .header("Authorization", "PortOne " + portOneProperties.getApi().getSecret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(BillingKeyPaymentResponse.class);

            log.info("빌링키 결제 응답 - paymentId: {}, status: {}",
                    response != null ? response.getPaymentId() : null,
                    response != null ? response.getStatus() : null);

            return response;

        } catch (Exception e) {
            log.error("빌링키 결제 실패 - paymentId: {}, billingKey: {}, error: {}",
                    paymentId, request.getBillingKey(), e.getMessage(), e);
            throw e;
        }
    }
}