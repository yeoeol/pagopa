package com.commerce.pagopa.identity.domain;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.USER_NOT_FOUND;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long userId);

    Optional<User> findByIdForUpdate(Long userId);

    Page<User> findAll(Pageable pageable);

    List<User> findByIdIn(Collection<Long> userIds);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    int bulkUnSuspend(UserStatus activeStatus, UserStatus suspendedStatus, LocalDateTime now, LocalDateTime threshold);

    Page<User> searchAdminUsers(String keyword, UserStatus status, RoleCode roleCode, Pageable pageable);

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
