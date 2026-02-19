package com.paymentteamproject.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 파싱 유틸리티
 * parseClaims()는 JWT 예외를 호출부로 그대로 전파합니다.
 * 예외 처리는 JwtAuthenticationFilter에서 케이스별로 수행합니다.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret:commercehub-secret-key-for-demo-please-change-this-in-production-environment}") String secret,
            @Value("${jwt.access-token-validity:1800}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity:604800}") long refreshTokenValidityInSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    /**
     * JWT 토큰 생성
     * <p>
     * TODO: 개선 사항
     * - 사용자 역할(Role) 정보 추가
     * - 추가 Claims 정보 (이름, 이메일 등)
     * - Refresh Token 발급 로직
     */
    public String createToken(String email, String name, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     * - Access Token보다 긴 유효기간
     * - 최소한의정보만 포함(이메일만)
     */
    public String createRefreshToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("type", "refresh")
                .claim("tokenId", UUID.randomUUID().toString()) //토큰 고유 식별자
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 클레임 파싱
     * 예외를 잡지 않고 호출부(JwtAuthenticationFilter)로 전파합니다.
     * - ExpiredJwtException     : 토큰 만료
     * - MalformedJwtException   : 잘못된 토큰 형식
     * - SignatureException      : 서명 불일치
     * - UnsupportedJwtException : 지원하지 않는 토큰 형식
     * - IllegalArgumentException: 빈 토큰 또는 null
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * JWT 토큰에서 사용자 이메일 추출
     */
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * JWT 토큰에서 사용자 역할 추출
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Refresh Token의 만료 시간 반환
     */
    public Instant getRefreshTokenExpiryDate() {
        return Instant.now().plusMillis(refreshTokenValidityInMilliseconds);
    }
}
