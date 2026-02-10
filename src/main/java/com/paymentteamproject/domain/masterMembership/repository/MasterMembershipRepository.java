package com.paymentteamproject.domain.masterMembership.repository;

import com.paymentteamproject.domain.masterMembership.entity.MasterMembership;
import com.paymentteamproject.domain.user.consts.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterMembershipRepository extends JpaRepository<MasterMembership, UserRank> {

    Optional<MasterMembership> findByMembership(UserRank membership);
}
