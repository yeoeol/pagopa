package com.commerce.pagopa.userrole.domain.repository;

import com.commerce.pagopa.userrole.domain.model.UserRole;

import java.util.Collection;
import java.util.List;

public interface UserRoleRepository {
	List<UserRole> findAllWithRoleByUserIds(Collection<Long> userIds);
}
