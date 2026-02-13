package com.paymentteamproject.domain.auth;

import com.paymentteamproject.domain.auth.service.AuthServiceSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthServiceSupportTest {

    @Test
    @DisplayName("인증 성공 시 인증 객체 반환")
    void validation_success(){
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        Authentication mockAuth = mock(Authentication.class);
        Object principal = new Object(); // 실제로는 CustomUserDetails 등이 들어감

        // 2. 가짜 동작: Principal이 존재함
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(principal);

        // 3. 실행
        Authentication result = AuthServiceSupport.validateRequest(authenticationManager, "test@email.com", "password");

        // 4. 검증
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(principal);

    }

    @Test
    @DisplayName("인증 객체의 Principal이 null이면 BadCredentialException이 발생한다.")
    void validation_fail_null_principal(){
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        Authentication mockAuth = mock(Authentication.class);

        // 2. 가짜 동작 설정: authenticate는 성공하지만 getPrincipal()은 null 반환
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(null);

        assertThatThrownBy(() -> AuthServiceSupport.validateRequest(authenticationManager, "test@email.com", "test_password"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Authentication principal is null");
    }
}

