package com.commerce.pagopa.identity.api;

import java.util.Collection;
import java.util.Map;

public interface UserApi {
    UserSummary get(Long userId);

    Map<Long, UserSummary> findAllByIdIn(Collection<Long> userIds);

    boolean existsById(Long userId);

    void grantSellerRole(Long userId);
}
