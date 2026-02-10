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

    /**
     * 토큰 값으로 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID로 삭제
     * 영속성 컨텍스트와의 정합성 문제, 예상치 못한 Select 쿼리 방지를 위해 벌크 삭제 쿼리 명시
     */
    @Modifying
    @Query("delete from RefreshToken r where r.user = :user")
    void deleteByUser(@Param("user") User user);

    /**
     * 토큰 값으로 삭제
     */
    @Modifying
    void deleteByToken(String token);

    /**
     * 만료된 토큰 삭제 (배치 작업용)
     */
    @Modifying
    void deleteByExpiryDateBefore(Instant now);


}
