package com.commerce.pagopa.discovery.application.command;

import java.time.LocalDateTime;

public record ProductSearchHistoryCommand(
        Long userId,
        String sessionId,
        String keyword,
        LocalDateTime searchedAt
) {
}
