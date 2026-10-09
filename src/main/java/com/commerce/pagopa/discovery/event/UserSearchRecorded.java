package com.commerce.pagopa.discovery.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSearchRecorded(
        UUID eventId,
        Long userId,
        String keyword,
        LocalDateTime searchedAt
) {
}
