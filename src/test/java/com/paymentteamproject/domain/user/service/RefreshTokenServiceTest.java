package com.paymentteamproject.domain.user.service;

import com.paymentteamproject.domain.auth.entity.RefreshToken;
import com.paymentteamproject.domain.auth.exception.TokenException;
import com.paymentteamproject.domain.auth.repository.RefreshTokenRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.repository.UserRepository;
import com.paymentteamproject.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("토큰 생성 성공: 기존 토큰 업데이트")
    void create_success_update() {
        // given
        String email = "example@sparta.com";
        User user = User.builder().username("홍길동").email(email).build();

        // 기존에 존재하던 토큰 (조회용)
        Instant oldExpiry = Instant.now().minusSeconds(60);
        RefreshToken oldToken = new RefreshToken("old_token", user, oldExpiry);

        // 새롭게 발급될 정보
        String newTokenValue = "new_token_value";
        Instant newExpiry = Instant.now().plusSeconds(3600);

        // Mock 대본 작성
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(jwtTokenProvider.createRefreshToken(email)).willReturn(newTokenValue);
        given(jwtTokenProvider.getRefreshTokenExpiryDate()).willReturn(newExpiry);
        given(refreshTokenRepository.findByUser(user)).willReturn(Optional.of(oldToken));

        // when: 실제 로직 실행
        RefreshToken result = refreshTokenService.createRefreshToken(email);

        // then: 결과 검증
        assertThat(result.getToken()).isEqualTo(newTokenValue);
        assertThat(result.getExpiryDate()).isEqualTo(newExpiry);
        assertThat(result.getUser()).isEqualTo(user);

        verify(refreshTokenRepository, never()).save(any());

    }

    @Test
    @DisplayName("토큰 생성 성공: 신규 토큰 추가")
    void create_success_create() {
        // given
        String email = "example@sparta.com";
        User user = User.builder().username("홍길동").email(email).build();

        // 새롭게 발급될 정보
        String newTokenValue = "new_token_value";
        Instant newExpiry = Instant.now().plusSeconds(3600);

        // Mock 대본 작성
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(jwtTokenProvider.createRefreshToken(email)).willReturn(newTokenValue);
        given(jwtTokenProvider.getRefreshTokenExpiryDate()).willReturn(newExpiry);

        // 기존 토큰이 없는 상황
        given(refreshTokenRepository.findByUser(user)).willReturn(Optional.empty());

        // save 호출 시 인자로 넘어온 객체를 그대로 반환하도록 설정
        given(refreshTokenRepository.save(any(RefreshToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        RefreshToken result = refreshTokenService.createRefreshToken(email);

        // then
        assertThat(result.getToken()).isEqualTo(newTokenValue);
        assertThat(result.getExpiryDate()).isEqualTo(newExpiry);
        assertThat(result.getUser()).isEqualTo(user);

        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰 생성 실패: 사용자를 찾을 수 없음")
    void create_fail_user_not_found() {
        // given
        String email = "notfound@email.com";
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(email))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다.");

        verify(jwtTokenProvider, never()).createRefreshToken(anyString());
        verify(jwtTokenProvider, never()).getRefreshTokenExpiryDate();
        verify(refreshTokenRepository, never()).findByUser(any(User.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    /**
     * Refresh Token 검증
     * - DB에서 토큰 조회 및 만료 여부 확인
     */
    @Test
    @DisplayName("토큰 검증 실패: 존재하지 않는 토큰")
    void token_not_found(){
        // given
        String notFoundToken = "notFoundToken";
        given(refreshTokenRepository.findByToken(notFoundToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken(notFoundToken))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("존재하지 않는 Refresh Token입니다.");

        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("토큰 검증 실패: 만료된 토큰")
    void token_expired(){
        // given
        String expiredTokenValue = "expired_token";
        Instant pastDate = Instant.now().minusSeconds(60);

        // 만료 시간이 과거인 토큰 생성
        RefreshToken expiredToken = RefreshToken.builder()
                .token(expiredTokenValue)
                .expiryDate(pastDate)
                .build();

        given(refreshTokenRepository.findByToken(expiredTokenValue)).willReturn(Optional.of(expiredToken));
        // when & then
        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken(expiredTokenValue))
                .isInstanceOf(TokenException.class)
                .hasMessageContaining("만료된 토큰입니다. 다시 로그인해주세요.");

        // 삭제를 호출했는지 확인
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

}
