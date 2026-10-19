package com.commerce.pagopa.assistant.ai.application.dto.response;

import com.commerce.pagopa.assistant.ai.domain.AssistantConversation;

public record AssistantConversationResponseDto(
        Long conversationId,
        Long userId,
        String title,
        String status
) {
    public static AssistantConversationResponseDto from(AssistantConversation conversation) {
        return new AssistantConversationResponseDto(
                conversation.getId(),
                conversation.getUserId(),
                conversation.getTitle(),
                conversation.getStatus().name()
        );
    }
}
