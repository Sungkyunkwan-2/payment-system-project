package com.paymentteamproject.common.service;

import com.paymentteamproject.common.dtos.auth.LoginRequest;
import com.paymentteamproject.common.dtos.auth.LoginResponse;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import com.paymentteamproject.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try{
            // 1. 이메일/비밀번호 검증
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 2. 인증 성공 시 JWT 토큰 생성
            String accessToken = jwtTokenProvider.createToken(auth.getName());


        } catch (AuthenticationException e){
            throw new RuntimeException("이메일 또는 비밀번호가 잘못되었습니다.");
        }

        // 4. 토큰 반환
        return new LoginResponse(user.getUsername(), user.getEmail());
    }
}
