package com.paymentteamproject.domain.masterMembership.Initializer;

import com.paymentteamproject.domain.masterMembership.entity.MasterMembership;
import com.paymentteamproject.domain.masterMembership.entity.MembershipStatus;
import com.paymentteamproject.domain.masterMembership.repository.MasterMembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipDataInitializer implements CommandLineRunner {

    private final MasterMembershipRepository masterMembershipRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 이미 데이터가 있으면 스킵
        if (masterMembershipRepository.count() > 0) {
            log.info("멤버십 데이터가 이미 존재합니다.");
            return;
        }

        log.info("멤버십 초기 데이터를 생성합니다.");

        // 4가지 멤버십 등급 초기화
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.BRONZE, 0.001));   // 0.1%
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.SILVER, 0.005));   // 0.5%
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.GOLD, 0.01));      // 1%
        masterMembershipRepository.save(new MasterMembership(MembershipStatus.DIAMOND, 0.03));   // 3%

        log.info("멤버십 초기 데이터 생성 완료");
    }
}
