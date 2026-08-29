package com.commerce.pagopa.user.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.UserStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.USER_NOT_FOUND;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByIdForUpdate(Long userId);

    Page<User> findAll(Pageable pageable);

    Optional<User> findByProviderAndProviderId(
            Provider provider,
            String providerId
    );

    int bulkUnSuspend(
            UserStatus activeStatus,
            UserStatus suspendedStatus,
            Instant now,
            Instant threshold
    );

    Page<User> searchAdminUsers(
            String keyword,
            UserStatus status,
            RoleCode roleCode,
            Pageable pageable
    );

    boolean existsById(Long userId);

    default User findByIdOrThrow(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    default User findByIdForUpdateOrThrow(Long userId) {
        return findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
