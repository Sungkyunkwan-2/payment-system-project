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
 * JWT 토큰 생성 및 검증 유틸리티
 * 개선할 부분: Refresh Token, Token Expiry 관리, Claims 커스터마이징 등
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
     * JWT 토큰에서 사용자 이메일 추출
     */
    public String getEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * JWT 토큰 유효성 검증
     * <p>
     * TODO: 개선 사항
     * - 토큰 블랙리스트 체크 (로그아웃된 토큰)
     * - 토큰 갱신 로직
     * - 상세한 예외 처리
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // TODO: 구체적인 예외 처리 구현
            // - ExpiredJwtException: 만료된 토큰
            // - MalformedJwtException: 잘못된 형식
            // - SignatureException: 서명 오류
            return false;
        }
    }

    /**
     * Refresh Token의 만료 시간 반환
     */
    public Instant getRefreshTokenExpiryDate() {
        return Instant.now().plusMillis(refreshTokenValidityInMilliseconds);
    }
}
