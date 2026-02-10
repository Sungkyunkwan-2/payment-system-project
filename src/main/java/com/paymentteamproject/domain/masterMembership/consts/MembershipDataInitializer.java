package com.paymentteamproject.domain.masterMembership.consts;

import com.paymentteamproject.domain.masterMembership.entity.MasterMembership;
import com.paymentteamproject.domain.masterMembership.repository.MasterMembershipRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MembershipDataInitializer {
    private final MasterMembershipRepository masterMembershipRepository;

    //TODO: RDB 적용 시 초기 값이 세팅 되어 있어 해당 클래스 불필요 삭제 바람
    @PostConstruct
    @Transactional
    public void initMemberships() {
        // 이미 데이터가 있으면 스킵
        if (masterMembershipRepository.count() > 0) {
            return;
        }

        // 4가지 멤버십 등급 초기화
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.BRONZE, 0.001));
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.SILVER, 0.005));
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.GOLD, 0.01));
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.DIAMOND, 0.03));
    }
}