package com.commerce.pagopa.domain.user.repository;

import com.commerce.pagopa.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
