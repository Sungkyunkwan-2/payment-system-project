package com.paymentteamproject.domain.user.service;

import com.paymentteamproject.common.dtos.ApiResponse;
import com.paymentteamproject.common.dtos.RegisterRequest;
import com.paymentteamproject.common.dtos.RegisterResponse;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exceptions.DuplicateEmailException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public RegisterResponse save(RegisterRequest request){
        // 이메일 중복 검사
        boolean duplicate = userRepository.existsByEmail(request.getEmail());
        if(duplicate) throw new DuplicateEmailException();
        // db에 저장
        User savedUser = new User(
                request.getUsername(),
                request.getPhone(),
                request.getEmail(),
                request.getPassword(),
                0
        );
        userRepository.save(savedUser);
        // 반환
        return new RegisterResponse(
                request.getUsername(),
                request.getEmail()
        );
    }
}
