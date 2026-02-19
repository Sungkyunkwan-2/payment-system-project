package com.paymentteamproject.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증 없이 보호된 리소스에 접근할 때 호출됩니다.
 * (토큰 자체가 없는 경우 — 토큰 파싱 오류는 JwtAuthenticationFilter에서 처리)
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"errorCode\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}"
        );
    }
}
