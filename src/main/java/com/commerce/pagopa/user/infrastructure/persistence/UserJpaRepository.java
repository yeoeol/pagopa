package com.commerce.pagopa.user.infrastructure.persistence;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {

    @Override
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    @Override
    @Modifying(clearAutomatically = true)
    @Query(value =
            "UPDATE User u " +
            "SET u.userStatus = 'ACTIVE', " +
                "u.banEndDate = NULL " +
            "WHERE u.banEndDate < :now " +
                "AND u.userStatus = 'BANNED'")
    void bulkUnbanBefore(@Param("now") LocalDateTime now);
}
