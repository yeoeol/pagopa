package com.commerce.pagopa.user.domain.repository;

import com.commerce.pagopa.user.domain.model.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    Optional<RefreshToken> findByToken(String token);
}
