package com.paymentteamproject.domain.webhook.webhooksecurity;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class PortOneWebhookVerifier {

    @Value("${portone.webhook.secret}")
    private String secret;

    @Value("${portone.webhook.secret-format:base64}")
    private String secretFormat;

    private byte[] signingKey;

    @PostConstruct
    void init() {
        this.signingKey = decodeSigningKey(secret, secretFormat);
    }

    public boolean verify(
            byte[] rawBody,
            String webhookId,
            String webhookTimestamp,
            String webhookSignature
    ) {
        if (rawBody == null ||
                webhookId == null ||
                webhookTimestamp == null ||
                webhookSignature == null ||
                signingKey == null) {
            return false;
        }

        webhookId = webhookId.trim();
        webhookTimestamp = webhookTimestamp.trim();
        webhookSignature = webhookSignature.trim();

        if (!webhookSignature.startsWith("v1,")) {
            return false;
        }

        byte[] toSign = buildToSign(webhookId, webhookTimestamp, rawBody);

        byte[] computedMac = hmacSha256(toSign, signingKey);

        byte[] givenMac;
        try {
            String givenBase64 = webhookSignature.substring(3).trim(); // "v1," 제거
            givenMac = Base64.getDecoder().decode(givenBase64);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return MessageDigest.isEqual(computedMac, givenMac);
    }

    private static byte[] buildToSign(String webhookId, String timestamp, byte[] body) {
        byte[] prefix = (webhookId + "." + timestamp + ".").getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[prefix.length + body.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(body, 0, result, prefix.length, body.length);
        return result;
    }

    private static byte[] hmacSha256(byte[] data, byte[] keyBytes) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute webhook signature", e);
        }
    }

    private static byte[] decodeSigningKey(String secret, String format) {
        if (secret == null) return null;

        String s = secret.trim();

        if ("raw".equalsIgnoreCase(format)) {
            return s.getBytes(StandardCharsets.UTF_8);
        }

        if (s.startsWith("whsec_")) {
            s = s.substring("whsec_".length());
        }

        return Base64.getDecoder().decode(s);
    }
}
