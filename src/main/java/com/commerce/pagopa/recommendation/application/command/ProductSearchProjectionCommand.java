package com.commerce.pagopa.recommendation.application.command;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public record ProductSearchProjectionCommand(
        UUID eventId,
        Long userId,
        String keyword,
        LocalDateTime searchedAt
) {
    public ProductSearchProjectionCommand {
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(userId);
        Objects.requireNonNull(keyword);
        Objects.requireNonNull(searchedAt);
    }
}
