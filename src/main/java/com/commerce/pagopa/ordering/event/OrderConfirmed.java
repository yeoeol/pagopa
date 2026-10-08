package com.commerce.pagopa.ordering.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderConfirmed(UUID eventId, Long orderId, Long userId, List<Long> productIds,
        LocalDateTime confirmedAt) {
    public OrderConfirmed {
        productIds = List.copyOf(productIds);
    }
}
