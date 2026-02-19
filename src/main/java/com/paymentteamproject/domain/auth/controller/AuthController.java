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
                ApiResponse.success(HttpStatus.CREATED, "회원가입에 성공했습니다.", userService.save(request))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenDto tokens = authService.login(request);
        CookieUtil.addCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                tokens.refreshToken(),
                REFRESH_TOKEN_COOKIE_MAX_AGE,
                cookieSecure
        );
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.accessToken())
                .header("Access-Control-Expose-Headers", "Authorization")
                .body(ApiResponse.success(HttpStatus.OK, "로그인에 성공했습니다", request.getEmail()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtil.getCookie(request, "refreshToken").orElse(null);
        authService.logout(token);
        CookieUtil.deleteCookie(response, REFRESH_TOKEN_COOKIE_NAME, cookieSecure);
        return ResponseEntity.ok().body(ApiResponse.success(HttpStatus.OK, "로그아웃되었습니다", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookie(request, REFRESH_TOKEN_COOKIE_NAME)
                .orElseThrow(() -> new TokenException("리프레시 토큰이 쿠키에 존재하지 않습니다."));
        TokenDto tokens = authService.refresh(refreshToken);
        CookieUtil.addCookie(
                response,
                REFRESH_TOKEN_COOKIE_NAME,
                tokens.refreshToken(),
                REFRESH_TOKEN_COOKIE_MAX_AGE,
                cookieSecure
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .header("Access-Control-Expose-Headers", "Authorization")
                .body(Map.of("success", true, "message", "토큰 재발급 성공"));
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUser(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok().body(userService.getCurrentUser(email));
    }
}
