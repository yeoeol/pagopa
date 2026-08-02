package com.commerce.pagopa.user.infrastructure.persistence;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Override
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    @Override
    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE User u " +
            "SET u.status = 'ACTIVE', " +
                "u.suspendedUntil = NULL " +
            "WHERE u.withdrawnAt IS NULL " +
                "AND u.suspendedUntil <= :now " +
                "AND u.status = 'SUSPENDED'")
    void bulkUnban(@Param("now") Instant now);
}
