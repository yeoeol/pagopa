package com.commerce.pagopa.identity.domain;

import java.util.Collection;
import java.util.List;

public interface UserRoleRepository {
    UserRole save(UserRole userRole);

    List<UserRole> findAllWithRoleByUserIds(Collection<Long> userIds);
}
