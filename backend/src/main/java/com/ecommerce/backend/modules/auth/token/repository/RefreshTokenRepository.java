package com.ecommerce.backend.modules.auth.token.repository;

import com.ecommerce.backend.modules.auth.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    /**
     * Найти токен по значению
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.isRevoked = false")
    Optional<RefreshToken> findValidToken(@Param("token") String token);

    /**
     * Найти все активные токены пользователя
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId);

    /**
     * Отозвать все токены пользователя
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true " +
            "WHERE rt.user.id = :userId AND rt.isRevoked = false")
    void revokeAllUserTokens(@Param("userId") Long userId);

    /**
     * Отозвать конкретный токен
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true " +
            "WHERE rt.token = :token")
    void revokeToken(@Param("token") String token);

    /**
     * Удалить истекшие токены (для cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.isRevoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    /**
     * Посчитать активные токены пользователя
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    long countValidTokensByUserId(@Param("userId") Long userId);

    /**
     * Найти старейшие токены пользователя для ограничения количества
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false " +
            "ORDER BY rt.createdAt ASC")
    List<RefreshToken> findOldestTokensByUserId(@Param("userId") Long userId);

}
