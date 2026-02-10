package com.paymentteamproject.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰 인증 필터
 * 모든 요청에서 JWT 토큰을 검증하고 SecurityContext에 인증 정보 설정
 *
 * TODO: 개선 사항
 * - 역할(Role) 정보를 토큰에서 추출
 * - 예외 처리 개선
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter (
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = getJwtFromRequest(request);

            // 2. 토큰 유효성 검증
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 3. 토큰에서 사용자 정보 추출
                String email = jwtTokenProvider.getEmail(token);

                //4. UserDetails 조회 추가
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 5. 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                        null,
                            userDetails.getAuthorities() //권한도 UserDetails에서 가져옴
                    );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 6. SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            log.error("토큰이 만료되었습니다. {}", e.getMessage());
            setErrorResponse(response, "TOKEN_EXPIRED", "토큰이 만료되었습니다. 갱신이 필요합니다.");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
            setErrorResponse(response, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
            return;
        } catch (Exception e) {
            log.error("JWT 인증 실패", e);
            // TODO: 구현 - 적절한 에러 응답
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 JWT 토큰 추출
     * Authorization: Bearer {token}
     */
    // resolveToken과 동일
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    // 응답을 JSON 형태로 만드는 편의 메서드
    private void setErrorResponse(HttpServletResponse response, String errorCode, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 세팅
        response.setContentType("application/json;charset=UTF-8");

        // 프론트엔드와 약속한 에러 포맷으로 전송
        String json = String.format("{\"success\":false, \"errorCode\":\"%s\", \"message\":\"%s\"}", errorCode, message);
        response.getWriter().write(json);
    }
}
