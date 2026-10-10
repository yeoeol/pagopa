package com.commerce.pagopa.catalog.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductSearched(
        UUID eventId,
        Long userId,
        String sessionId,
        String keyword,
        LocalDateTime searchedAt
) {
}
