package com.commerce.pagopa.user.infrastructure.persistence;

import com.commerce.pagopa.role.domain.model.enums.RoleCode;
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
    @Query("""
            UPDATE User u
            SET u.status = :activeStatus,
                u.statusChangedAt = :now
            WHERE u.status = :suspendedStatus
              AND u.statusChangedAt <= :threshold
            """)
    int bulkUnSuspend(
            @Param("activeStatus") UserStatus activeStatus,
            @Param("suspendedStatus") UserStatus suspendedStatus,
            @Param("now") Instant now,
            @Param("threshold") Instant threshold
    );

    @Override
    @Query("""
            SELECT u
            FROM User u
            WHERE (:keyword IS NULL
                OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR u.status = :status)
              AND (:roleCode IS NULL OR EXISTS (
                    SELECT ur.id
                    FROM UserRole ur
                    JOIN ur.role r
                    WHERE ur.user = u
                        AND r.code = :roleCode
                        AND r.enabled = true
              ))
            """)
    Page<User> searchAdminUsers(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            @Param("roleCode") RoleCode roleCode,
            Pageable pageable
    );
}
