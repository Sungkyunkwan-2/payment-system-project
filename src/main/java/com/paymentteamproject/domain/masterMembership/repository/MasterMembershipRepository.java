package com.paymentteamproject.domain.masterMembership.repository;

import com.paymentteamproject.domain.masterMembership.consts.MembershipStatus;
import com.paymentteamproject.domain.masterMembership.entity.MasterMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterMembershipRepository extends JpaRepository<MasterMembership, MembershipStatus> {

    Optional<MasterMembership> findByMembership(MembershipStatus membership);
}
