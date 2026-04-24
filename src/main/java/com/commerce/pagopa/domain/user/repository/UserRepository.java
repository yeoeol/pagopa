package com.commerce.pagopa.domain.user.repository;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.entity.enums.Provider;
import com.commerce.pagopa.domain.user.entity.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByEmail(String email);

    List<User> findByBanEndDateAfterAndUserStatus(LocalDateTime now, UserStatus userStatus);

    List<User> findByBanEndDateIsBeforeAndUserStatus(LocalDateTime now, UserStatus userStatus);
}
