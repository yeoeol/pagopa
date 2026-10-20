package com.commerce.pagopa.assistant.chat.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantConversationRequestDto(
        @NotBlank @Size(max = 4000) String message
) {
}
