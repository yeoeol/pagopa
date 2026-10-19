package com.commerce.pagopa.assistant.ai.application.dto.response;

public record AssistantMessageResponseDto(
        Long conversationId,
        Long userMessageId,
        Long assistantMessageId,
        String content,
        Long inputTokens,
        Long outputTokens
) {
}
