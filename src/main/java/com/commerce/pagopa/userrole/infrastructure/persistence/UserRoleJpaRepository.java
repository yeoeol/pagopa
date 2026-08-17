package com.commerce.pagopa.userrole.infrastructure.persistence;

import com.commerce.pagopa.userrole.domain.model.UserRole;
import com.commerce.pagopa.userrole.domain.repository.UserRoleRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleJpaRepository extends JpaRepository<UserRole, Long>, UserRoleRepository {

}
