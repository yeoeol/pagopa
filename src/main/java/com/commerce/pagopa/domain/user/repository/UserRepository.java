package com.commerce.pagopa.domain.user.repository;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.entity.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    @Modifying
    @Query(value =
            "UPDATE User u " +
            "SET u.userStatus = 'ACTIVE', " +
                "u.banEndDate = NULL " +
            "WHERE u.banEndDate < :now " +
                "AND u.userStatus = 'BANNED'")
    void bulkUnbanBefore(@Param("now") LocalDateTime now);
}
