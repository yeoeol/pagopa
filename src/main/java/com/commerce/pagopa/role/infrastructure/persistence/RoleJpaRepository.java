package com.commerce.pagopa.role.infrastructure.persistence;

import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.repository.RoleRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<Role, Long>, RoleRepository {
}
