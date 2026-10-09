package com.commerce.pagopa.recommendation.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecommendationEvent(
        UUID eventId,
        RecommendationEventType eventType,
        Long userId,
        Long aggregateId,
        Payload payload,
        LocalDateTime occurredAt
) {
    public sealed interface Payload permits SearchPayload, OrderPayload {
    }

    public record SearchPayload(String keyword) implements Payload {
    }

    public record OrderPayload(List<Long> productIds) implements Payload {
        public OrderPayload {
            productIds = List.copyOf(productIds);
        }
    }
}
