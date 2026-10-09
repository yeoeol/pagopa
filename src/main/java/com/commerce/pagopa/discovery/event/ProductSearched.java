package com.commerce.pagopa.discovery.event;

public record ProductSearched(
        Long userId,
        String sessionId,
        String keyword
) {
}
