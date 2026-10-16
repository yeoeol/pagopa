package com.commerce.pagopa.assistant.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantMessageRequestDto(
        @NotBlank @Size(max = 4000) String message
) {
}
