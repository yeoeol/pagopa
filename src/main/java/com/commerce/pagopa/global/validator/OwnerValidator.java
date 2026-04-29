package com.commerce.pagopa.global.validator;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public abstract class OwnerValidator<T> {

    @Transactional(readOnly = true)
    public boolean isOwner(Long resourceId, Long userId) {
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

    protected abstract Optional<T> findResource(Long resourceId);

    protected abstract Long extractOwnerId(T resource);
}
