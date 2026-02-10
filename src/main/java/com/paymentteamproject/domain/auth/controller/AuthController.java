package com.paymentteamproject.domain.auth.controller;

import com.paymentteamproject.common.dto.ApiResponse;
import com.paymentteamproject.domain.auth.dto.*;
import com.paymentteamproject.domain.auth.exception.TokenException;
import com.paymentteamproject.domain.auth.service.AuthService;
import com.paymentteamproject.domain.auth.util.CookieUtil;
import com.paymentteamproject.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

/**
 * 인증 관련 API 컨트롤러
 * 구현할 API 엔드포인트 템플릿
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7일

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        HttpStatus.CREATED, "회원가입에 성공했습니다.", userService.save(request)
                )
        );
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        // 비즈니스 로직 처리 (Service에 위임)
        TokenDto tokens = authService.login(request);

        // Refresh Token 쿠키 설정 (CookieUtil 사용)
        CookieUtil.addCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                tokens.refreshToken(),
                REFRESH_TOKEN_COOKIE_MAX_AGE,
                cookieSecure
        );

        // 응답 메시지 구성 (Access Token은 헤더에 담아 응답)

        // 4. Access Token을 헤더에 담아 응답
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.accessToken())
                // CORS 환경에서 프론트엔드가 Authorization 헤더에 접근할 수 있도록 노출
                .header("Access-Control-Expose-Headers", "Authorization")
                .body(ApiResponse.success(
                        HttpStatus.OK, "로그인에 성공했습니다", request.getEmail())
                );
    }

    /**
     * 토큰 재발급 API
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 리프레시 토큰 추출
        String refreshToken = CookieUtil.getCookie(request, REFRESH_TOKEN_COOKIE_NAME)
                .orElseThrow(() -> new TokenException("리프레시 토큰이 쿠키에 존재하지 않습니다."));

        // 2. 서비스 로직 호출 (새 토큰 세트 생성)
        TokenDto tokens = authService.refresh(refreshToken);

        // 3. 새로운 Refresh Token을 쿠키에 업데이트
        CookieUtil.addCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                tokens.refreshToken(),
                REFRESH_TOKEN_COOKIE_MAX_AGE,
                cookieSecure
        );

        // 4. 새로운 Access Token을 헤더에 담아 응답
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .header("Access-Control-Expose-Headers", "Authorization")
                .body(Map.of("success", true, "message", "토큰 재발급 성공"));
    }

    /**
     * 현재 로그인한 사용자 정보 조회 API
     * GET /api/auth/me
     *
     * 응답:
     * {
     *   "success": true,
     *   "email": "user@example.com",
     *   "customerUid": "CUST_xxxxx",
     *   "name": "홍길동"
     * }
     *
     * 중요: customerUid는 PortOne 빌링키 발급 시 활용!
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUser(Principal principal) {

        String email = principal.getName();

        return ResponseEntity.ok().body(userService.getCurrentUser(email));
    }
}
