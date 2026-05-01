package com.commerce.pagopa.scrap.application.dto.request;

import com.commerce.pagopa.scrap.domain.model.EntityType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScrapAddRequestDto(
        @NotNull(message = "{validation.notNull}")
        EntityType targetType,

        @NotNull(message = "{validation.notNull}")
        @Min(value = 1, message = "{validation.min}")
        Long targetId
) {
}
