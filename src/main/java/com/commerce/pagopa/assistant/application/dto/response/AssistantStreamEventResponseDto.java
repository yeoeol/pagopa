package com.commerce.pagopa.assistant.application.dto.response;

public record AssistantStreamEventResponseDto(
        String type,
        Long conversationId,
        Long userMessageId,
        Long assistantMessageId,
        String content,
        Long inputTokens,
        Long outputTokens,
        String errorCode
) {

    public static AssistantStreamEventResponseDto started(
            Long conversationId,
            Long userMessageId
    ) {
        return new AssistantStreamEventResponseDto(
                "started",
                conversationId,
                userMessageId,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static AssistantStreamEventResponseDto delta(
            String content
    ) {
        return new AssistantStreamEventResponseDto(
                "delta",
                null,
                null,
                null,
                content,
                null,
                null,
                null
        );
    }

    public static AssistantStreamEventResponseDto completed(
            Long conversationId,
            Long userMessageId,
            Long assistantMessageId,
            long inputTokens,
            long outputTokens
    ) {
        return new AssistantStreamEventResponseDto(
                "completed",
                conversationId,
                userMessageId,
                assistantMessageId,
                null,
                inputTokens,
                outputTokens,
                null
        );
    }

    public static AssistantStreamEventResponseDto failed(
            String errorCode
    ) {
        return new AssistantStreamEventResponseDto(
                "failed",
                null,
                null,
                null,
                null,
                null,
                null,
                errorCode
        );
    }
}
