package com.commerce.pagopa.user.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
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

    Optional<User> findById(Long id);

    Optional<User> findByIdForUpdate(Long id);

    Page<User> findAll(Pageable pageable);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    int bulkUnSuspend(
            UserStatus activeStatus,
            UserStatus suspendedStatus,
            Instant now,
            Instant threshold
    );

    Page<User> searchAdminUsers(String keyword, UserStatus status, Pageable pageable);

    default User findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    default User findByIdForUpdateOrThrow(Long id) {
        return findByIdForUpdate(id).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
