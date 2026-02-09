package com.paymentteamproject.domain.auth.controller;

import com.paymentteamproject.common.dtos.ApiResponse;
import com.paymentteamproject.domain.auth.dto.LoginRequest;
import com.paymentteamproject.domain.auth.dto.RegisterRequest;
import com.paymentteamproject.domain.auth.dto.RegisterResponse;
import com.paymentteamproject.domain.auth.service.AuthService;
import com.paymentteamproject.domain.user.service.UserService;
import com.paymentteamproject.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증 관련 API 컨트롤러
 * 구현할 API 엔드포인트 템플릿
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AuthService authService;

    /**
     * 로그인 API
     * POST /api/auth/login
     *
     * 요청 본문:
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     *
     * 응답 헤더:
     * Authorization: Bearer eyJhbGc...
     *
     * 응답 본문:
     * {
     *   "success": true,
     *   "email": "user@example.com"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        HttpStatus.CREATED, "회원가입에 성공했습니다.", userService.save(request)
                )
        );
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            String token = authService.login(request);
            response.put("success", true);
            response.put("email", request.getEmail());

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + token)
                    .body(response);
        } catch (AuthenticationException e) {
            // 인증 실패
            response.put("success", false);
            response.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
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
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {

        String email = principal.getName();

        // TODO: 구현
        // 데이터베이스에서 사용자 정보 조회
        // customerUid 생성은 조회 한 사용자 정보로 조합하여 생성, 추천 조합 : CUST_{userId}_{rand6:난수}
        // 임시 구현
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("email", email);
        response.put("customerUid", "CUST_" + Math.abs(email.hashCode()));  // PortOne 고객 UID
        response.put("name", email.split("@")[0]);  // 이메일에서 이름 추출
        response.put("phone", "010-0000-0000");  // Kg 이니시스 전화번호 필수
        response.put("pointBalance", 1000L);  // 포인트 잔액

        return ResponseEntity.ok(response);
    }
}
