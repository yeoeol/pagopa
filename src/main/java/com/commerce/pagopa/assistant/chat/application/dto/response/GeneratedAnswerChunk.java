package com.commerce.pagopa.assistant.chat.application.dto.response;

public record GeneratedAnswerChunk(
        String content,
        long inputTokens,
        long outputTokens
) {
}
