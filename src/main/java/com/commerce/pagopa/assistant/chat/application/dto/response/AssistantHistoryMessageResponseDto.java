package com.commerce.pagopa.assistant.chat.application.dto.response;

import java.time.LocalDateTime;

public record AssistantHistoryMessageResponseDto(
        Long conversationId,
        Long messageId,
        String role,
        String content,
        Long inputTokens,
        Long outputTokens,
        LocalDateTime createdAt
) {
}
