package com.commerce.pagopa.domain.scrap.dto.request;

import com.commerce.pagopa.domain.scrap.entity.enums.EntityType;
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
