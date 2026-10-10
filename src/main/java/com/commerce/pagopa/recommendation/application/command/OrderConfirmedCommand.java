package com.commerce.pagopa.recommendation.application.command;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderConfirmedCommand(
        UUID eventId,
        Long orderId,
        Long userId,
        List<Long> productIds,
        LocalDateTime confirmedAt
) {
}
