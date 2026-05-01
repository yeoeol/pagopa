package com.commerce.pagopa.user.domain.repository;

import com.commerce.pagopa.global.exception.UserNotFoundException;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Page<User> findAll(Pageable pageable);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    void bulkUnbanBefore(LocalDateTime now);

    default User findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(UserNotFoundException::new);
    }
}
