package com.paymentteamproject.domain.membershipTransaction.service;

import com.paymentteamproject.domain.membershipTransaction.consts.MembershipStatus;
import com.paymentteamproject.domain.membershipTransaction.entity.MembershipHistory;
import com.paymentteamproject.domain.membershipTransaction.repository.MembershipHistoryRepository;
import com.paymentteamproject.domain.user.entity.User;
import com.paymentteamproject.domain.user.exception.UserNotFoundException;
import com.paymentteamproject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserRepository userRepository;
    private final MembershipHistoryRepository membershipHistoryRepository;

    @Transactional
    public void refreshMembership(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));

        BigDecimal totalSpend = user.getTotalSpend() == null ? BigDecimal.ZERO : user.getTotalSpend();
        MembershipStatus newStatus = MembershipStatus.getAvailableStatus(totalSpend);

        MembershipHistory current = membershipHistoryRepository
                .findFirstByUser_IdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .orElse(null);

        if (current != null && current.getMembershipStatus() == newStatus) return;

        if (current != null) current.softDelete();

        membershipHistoryRepository.save(new MembershipHistory(user, newStatus));
    }
}