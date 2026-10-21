package com.commerce.pagopa.assistant.chat.application.dto.response;

import java.util.List;

public record AssistantConversationPageResponseDto(
        List<AssistantConversationResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        int startPage,
        int endPage
) {
}
