package com.paymentteamproject.config;

import com.paymentteamproject.security.JwtAuthenticationEntryPoint;
import com.paymentteamproject.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 2. CORS 설정 연결
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 예외 처리 핸들러 등록
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Session 사용 안 함 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 요청 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 1) 정적 리소스
                        .requestMatchers(toStaticResources().atCommonLocations()).permitAll()
                        // 2) 헬스 체크 API
                        .requestMatchers("/actuator/**").permitAll()
                        // 3) 템플릿 페이지 렌더링
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/pages/**").permitAll()
                        // 4) 공개 API
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        // 5) 인증 API
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/portone-webhook").permitAll()
                        // 6) 그 외 API는 인증 필요
                        .requestMatchers("/api/**").authenticated()
                        // 7) 나머지 전부 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/portone-webhook");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (프론트엔드 URL)
        // 개발 환경
//        configuration.setAllowedOrigins(Arrays.asList(
//                "http://localhost:3000",
//                "http://localhost:8080",
//                "http://127.0.0.1:3000"
//        ));
        // 프로덕션 환경에서는 실제 도메인으로 변경
        configuration.setAllowedOrigins(Arrays.asList("https://jihyeon1346.xyz"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 쿠키 전송 허용 (Refresh Token 쿠키를 위해 필수!)
        configuration.setAllowCredentials(true);

        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
