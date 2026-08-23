package com.commerce.pagopa.userrole.infrastructure.persistence;

import com.commerce.pagopa.userrole.domain.model.UserRole;
import com.commerce.pagopa.userrole.domain.repository.UserRoleRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserRoleJpaRepository extends JpaRepository<UserRole, Long>, UserRoleRepository {

	@Override
	@Query("""
			SELECT ur
			FROM UserRole ur
			JOIN FETCH ur.role r
			WHERE ur.user.id IN :userIds
			ORDER BY ur.user.id, r.code
			""")
	List<UserRole> findAllWithRoleByUserIds(
			@Param("userIds") Collection<Long> userIds
	);

}
