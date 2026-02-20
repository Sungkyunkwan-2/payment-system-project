package com.paymentteamproject.common.controller;

import com.paymentteamproject.common.dto.PublicConfigResponse;
import com.paymentteamproject.config.AppProperties;
import com.paymentteamproject.config.ClientApiProperties;
import com.paymentteamproject.config.PortOneProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class ConfigController {

    private final PortOneProperties portOneProperties;
    private final ClientApiProperties clientApiProperties;
    private final AppProperties appProperties;

    @GetMapping("/config")
    public ResponseEntity<PublicConfigResponse> getPublicConfig() {
        PublicConfigResponse response = PublicConfigResponse.builder()
                .portone(PublicConfigResponse.PortOneConfig.builder()
                        .storeId(portOneProperties.getStore().getId())
                        .channelKeys(portOneProperties.getChannel())
                        .build())
                .api(PublicConfigResponse.ClientApiConfig.builder()
                        .baseUrl(clientApiProperties.getBaseUrl())
                        .endpoints(clientApiProperties.getEndpoints())
                        .build())
                .branding(PublicConfigResponse.BrandingConfig.builder()
                        .appName(appProperties.getAppName())
                        .tagline(appProperties.getTagline())
                        .logoText(appProperties.getLogoText())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }
}
