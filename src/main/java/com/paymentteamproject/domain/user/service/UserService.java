package com.paymentteamproject.domain.user.service;

import com.paymentteamproject.domain.auth.dto.ProfileResponse;
import com.paymentteamproject.domain.auth.dto.RegisterRequest;
import com.paymentteamproject.domain.auth.dto.RegisterResponse;
import com.paymentteamproject.domain.membershipTransaction.consts.MembershipStatus;
import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import com.paymentteamproject.domain.membershipTransaction.repository.MembershipHistoryRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.DuplicateEmailException;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipHistoryRepository membershipHistoryRepository;

    @Transactional
    public RegisterResponse save(RegisterRequest request) {

        boolean duplicate = userRepository.existsByEmail(request.getEmail());
        if (duplicate) throw new DuplicateEmailException();

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder().username(request.getName()).phone(request.getPhone()).email(request.getEmail()).password(encodedPassword).pointBalance(BigDecimal.ZERO).build();

        User savedUser = userRepository.save(user);

        MembershipHistory membershipHistory = new MembershipHistory(savedUser, MembershipStatus.BRONZE);

        membershipHistoryRepository.save(membershipHistory);

        return new RegisterResponse(savedUser.getUsername(), savedUser.getEmail());
    }

    @Transactional(readOnly = true)
    public ProfileResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        return ProfileResponse.builder().email(user.getEmail()).customerUid("CUST_" + Math.abs(email.hashCode())).name(user.getUsername()).phone(user.getPhone()).pointBalance(user.getPointBalance()).build();
    }
}
