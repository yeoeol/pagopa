package com.commerce.pagopa.user.infrastructure.persistence;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Override
    @Query("""
            SELECT u
            FROM User u
                LEFT JOIN FETCH u.userRoles ur
                LEFT JOIN FETCH ur.role r
            WHERE u.provider = :provider
                AND u.providerId = :providerId
            """)
    Optional<User> findByProviderAndProviderId(
            @Param("provider") Provider provider,
            @Param("providerId") String providerId
    );

    @Override
    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE User u " +
            "SET u.status = 'ACTIVE', " +
                "u.suspendedUntil = NULL " +
            "WHERE u. IS NULL " +
                "AND u.suspendedUntil <= :now " +
                "AND u.status = 'SUSPENDED'")
    void bulkUnban(@Param("now") Instant now);

    @Override
    @Query("""
            SELECT u
            FROM User u
            WHERE (:keyword IS NULL
                OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR u.status = :status)
            """)
    Page<User> searchAdminUsers(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
