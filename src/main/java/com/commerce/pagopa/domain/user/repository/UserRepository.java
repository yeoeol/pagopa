package com.commerce.pagopa.domain.user.repository;

import com.commerce.pagopa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
