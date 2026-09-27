package com.commerce.pagopa.identity.infrastructure.persistence;

import com.commerce.pagopa.identity.domain.Role;
import com.commerce.pagopa.identity.domain.RoleRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleJpaRepository extends JpaRepository<Role, Long>, RoleRepository {

	@Override
	@Query("""
			SELECT r
			FROM Role r
			WHERE r.enabled = :enabled
			""")
	List<Role> findAllByEnabled(@Param("enabled") boolean enabled);

}
