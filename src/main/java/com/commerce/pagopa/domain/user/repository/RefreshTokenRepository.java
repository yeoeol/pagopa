package com.commerce.pagopa.domain.user.repository;

import com.commerce.pagopa.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    Optional<RefreshToken> findByToken(String token);
}
