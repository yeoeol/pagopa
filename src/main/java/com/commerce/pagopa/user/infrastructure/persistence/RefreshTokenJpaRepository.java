package com.commerce.pagopa.user.infrastructure.persistence;

import com.commerce.pagopa.user.domain.model.RefreshToken;
import com.commerce.pagopa.user.domain.repository.RefreshTokenRepository;

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
