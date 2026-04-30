package com.commerce.pagopa.global.validator;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public abstract class OwnerValidator<T, ID> {

    @Transactional(readOnly = true)
    public boolean isOwner(ID resourceId, Long userId) {
        if (resourceId == null || userId == null) {
            return false;
        }
        return findResource(resourceId)
                .map(resource -> {
                    Long ownerId = extractOwnerId(resource);
                    return ownerId != null && ownerId.equals(userId);
                })
                .orElse(false);
    }

    protected abstract Optional<T> findResource(ID resourceId);

    protected abstract Long extractOwnerId(T resource);
}
