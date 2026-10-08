package com.commerce.pagopa.identity.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commerce.pagopa.identity.domain.Role;
import com.commerce.pagopa.identity.domain.RoleRepository;

public interface RoleJpaRepository extends JpaRepository<Role, Long>, RoleRepository {

    @Override
    @Query("""
            SELECT r
            FROM Role r
            WHERE r.enabled = :enabled
            """)
    List<Role> findAllByEnabled(@Param("enabled") boolean enabled);
}
