package com.commerce.pagopa.role.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
	Role save(Role role);

	List<Role> findAll();

	List<Role> findAllByEnabled(boolean enabled);

	Optional<Role> findById(Long roleId);

	default Role findByIdOrThrow(Long roleId) {
		return findById(roleId).orElseThrow(
				() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)
		);
	}

	Optional<Role> findByCode(RoleCode code);
}
