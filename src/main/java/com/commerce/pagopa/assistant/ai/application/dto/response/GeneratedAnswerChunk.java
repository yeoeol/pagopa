package com.commerce.pagopa.assistant.ai.application.dto.response;

public record GeneratedAnswerChunk(
        String content,
        long inputTokens,
        long outputTokens
) {
}
