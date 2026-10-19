package com.commerce.pagopa.assistant.ai.application.dto.response;

public record GeneratedAnswer(
        String content,
        long inputTokens,
        long outputTokens
) {
}
