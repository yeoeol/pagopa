package com.commerce.pagopa.identity.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileUpdated(
        UUID eventId,
        String name,
        String oldProfileImageUrl,
        String newProfileImageUrl,
        LocalDateTime updatedAt
) {
}
