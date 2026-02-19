package com.paymentteamproject.domain.auth.repository;

import com.paymentteamproject.domain.auth.entity.RefreshToken;
import com.paymentteamproject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    @Modifying
    @Query("delete from RefreshToken r where r.user = :user")
    void deleteByUser(@Param("user") User user);

    @Modifying
    void deleteByToken(String token);

    @Modifying
    void deleteByExpiryDateBefore(Instant now);


}
