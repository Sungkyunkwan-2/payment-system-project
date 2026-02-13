package com.paymentteamproject.domain.user.service;


import com.paymentteamproject.domain.auth.dto.ProfileResponse;
import com.paymentteamproject.domain.auth.dto.RegisterRequest;
import com.paymentteamproject.domain.auth.dto.RegisterResponse;
import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import com.paymentteamproject.domain.membershipTransaction.repository.MembershipHistoryRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.DuplicateEmailException;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MembershipHistoryRepository membershipHistoryRepository;

    @InjectMocks
    private UserService userService;  // 테스트 대상 서비스

    @Test
    @DisplayName("회원가입 성공: 유효한 정보로 가입하면 유저와 멤버십 히스토리가 저장된다.")
    void save_success(){

        // given: 유효한 정보로 가입
        RegisterRequest request = RegisterRequest.builder()
                .name("테스터")
                .email("test@example.com")
                .password("password1234")
                .phone("010-1234-5678")
                .build();
            // duplicate == false
        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encoded_password");

        // Mock User 객체 생성 (save 호출 시 반환될 객체)
        User savedUser = User.builder()
                .username(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when
        RegisterResponse response = userService.save(request);

        // then
        assertThat(response.getUsername()).isEqualTo("테스터");
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        // 로직 검증: 각 레포지토리가 정확히 호출되었는지 확인
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(membershipHistoryRepository, times(1)).save(any(MembershipHistory.class));

    }

    @Test
    @DisplayName("회원가입 실패: 중복된 이메일이 존재하면 DuplicateEmailException이 발생")
    void save_fail_duplicate_email() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .name("테스터")
                .email("test@example.com")
                .password("password1234")
                .phone("010-1234-5678")
                .build();

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(DuplicateEmailException.class);

        // 중복 시 이후 로직(암호화, 저장)이 실행되지 않았음을 검증
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(membershipHistoryRepository, never()).save(any(MembershipHistory.class));
    }


    @Test
    @DisplayName("성공: 존재하는 이메일로 조회 시 프로필 정보를 정확히 반환한다.")
    void get_current_user() {


        // 입력된 이메일에 대해 항상 동일한 customerUid가 생성되는가?
        // User 엔티티의 필드들이 ProfileResponse로 누락 없이 전달되는가?

        // given
        String email = "test@example.com";

        User user = User.builder()
                .email(email)
                .username("홍길동")
                .phone("010-1111-2222")
                .pointBalance(BigDecimal.valueOf(1000))
                .build();

        // user가 담겨 있는 Optional을 return하도록 설정
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        ProfileResponse response = userService.getCurrentUser(email);

        // then
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getPhone()).isEqualTo("010-1111-2222");
        assertThat(response.getPointBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        assertThat(response.getCustomerUid()).startsWith("CUST_");
        assertThat(response.getCustomerUid()).isEqualTo("CUST_" + Math.abs(email.hashCode()));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일인 경우 UserNotFoundException이 발생한다.")
    void getCurrentUser_NotFound() {
        // given
        String email = "non-existent@example.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getCurrentUser(email))
                .isInstanceOf(UserNotFoundException.class);
    }

}
