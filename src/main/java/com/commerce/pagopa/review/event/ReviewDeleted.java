package com.commerce.pagopa.review.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReviewDeleted(
        UUID eventId,
        Long reviewId,
        List<String> imageUrls,
        LocalDateTime deletedAt
) {
    public ReviewDeleted {
        imageUrls = List.copyOf(imageUrls);
    }
}
