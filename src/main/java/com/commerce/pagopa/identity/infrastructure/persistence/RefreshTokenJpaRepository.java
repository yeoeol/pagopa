package com.commerce.pagopa.identity.infrastructure.persistence;

import com.commerce.pagopa.identity.domain.RefreshToken;
import com.commerce.pagopa.identity.domain.RefreshTokenRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenRepository {

    @Override
    Optional<RefreshToken> findByUserId(Long userId);

    @Override
    void deleteByUserId(Long userId);

    @Override
    Optional<RefreshToken> findByToken(String token);
}
