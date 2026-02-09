package com.paymentteamproject.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final PortOneProperties properties;

    @Bean
    public RestClient portOneRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getApi().getBaseUrl())
                .defaultHeader("Authorization", "PortOne " + properties.getApi().getSecret().trim())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
