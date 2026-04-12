package com.commerce.pagopa.domain.scrap.dto.request;

import com.commerce.pagopa.domain.scrap.entity.enums.EntityType;
import jakarta.validation.constraints.NotNull;

public record ScrapAddRequestDto(
        @NotNull(message = "{validation.notNull}")
        EntityType targetType,

        @NotNull(message = "{validation.notNull}")
        Long targetId
) {
}
