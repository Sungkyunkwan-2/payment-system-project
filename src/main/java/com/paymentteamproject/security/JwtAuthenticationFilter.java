package com.paymentteamproject.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 토큰 인증 필터 (Stateless)
 * DB 조회 없이 토큰 클레임만으로 인증 객체를 구성합니다.
 * 토큰 파싱 예외는 케이스별로 처리하여 명확한 에러 응답을 반환합니다.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = getJwtFromRequest(request);

            if (token != null) {
                // parseClaims()는 예외를 전파하므로 아래 catch 블록에서 처리됩니다.
                Claims claims = jwtTokenProvider.parseClaims(token);

                String email = claims.getSubject();
                String role  = claims.get("role", String.class);

                if (role == null || role.isBlank()) {
                    setErrorResponse(response, "INVALID_TOKEN", "토큰에 권한 정보가 없습니다.");
                    return;
                }

                // DB 조회 없이 클레임 정보만으로 UserDetails 구성
                UserDetails principal = User.withUsername(email)
                        .password("")
                        .authorities(new SimpleGrantedAuthority(role))
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (ExpiredJwtException e) {
            log.warn("[JWT] 만료된 토큰 - uri: {}, message: {}", request.getRequestURI(), e.getMessage());
            setErrorResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다. 갱신이 필요합니다.");
            return;

        } catch (MalformedJwtException e) {
            log.warn("[JWT] 잘못된 토큰 형식 - uri: {}, message: {}", request.getRequestURI(), e.getMessage());
            setErrorResponse(response, "MALFORMED_TOKEN", "토큰 형식이 올바르지 않습니다.");
            return;

        } catch (SignatureException e) {
            log.warn("[JWT] 토큰 서명 불일치 - uri: {}, message: {}", request.getRequestURI(), e.getMessage());
            setErrorResponse(response, "INVALID_SIGNATURE", "토큰 서명이 유효하지 않습니다.");
            return;

        } catch (UnsupportedJwtException e) {
            log.warn("[JWT] 지원하지 않는 토큰 형식 - uri: {}, message: {}", request.getRequestURI(), e.getMessage());
            setErrorResponse(response, "UNSUPPORTED_TOKEN", "지원하지 않는 토큰 형식입니다.");
            return;

        } catch (IllegalArgumentException e) {
            log.warn("[JWT] 빈 토큰 또는 잘못된 인자 - uri: {}, message: {}", request.getRequestURI(), e.getMessage());
            setErrorResponse(response, "EMPTY_TOKEN", "토큰이 비어있거나 잘못된 형식입니다.");
            return;

        } catch (Exception e) {
            log.error("[JWT] 인증 처리 중 예상치 못한 오류 - uri: {}", request.getRequestURI(), e);
            setErrorResponse(response, "AUTHENTICATION_ERROR", "인증 처리 중 오류가 발생했습니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 401 JSON 에러 응답 전송
     */
    private void setErrorResponse(HttpServletResponse response, String errorCode, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = String.format(
                "{\"success\":false,\"errorCode\":\"%s\",\"message\":\"%s\"}",
                errorCode, message
        );
        response.getWriter().write(json);
    }
}
