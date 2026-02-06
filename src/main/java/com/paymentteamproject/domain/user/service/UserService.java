package com.paymentteamproject.domain.user.service;

import com.paymentteamproject.common.dtos.auth.RegisterRequest;
import com.paymentteamproject.common.dtos.auth.RegisterResponse;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exceptions.DuplicateEmailException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse save(RegisterRequest request){
        // 이메일 중복 검사
        boolean duplicate = userRepository.existsByEmail(request.getEmail());
        if(duplicate) throw new DuplicateEmailException();

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // db에 저장
        User user = User.builder()
                .username(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(encodedPassword)
                .pointBalance(0)
                .build();

        User savedUser = userRepository.save(user);

        // 반환
        return new RegisterResponse(
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }
}
