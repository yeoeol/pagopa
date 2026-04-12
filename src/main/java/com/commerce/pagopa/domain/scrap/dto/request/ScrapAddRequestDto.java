package com.commerce.pagopa.domain.scrap.dto.request;

import com.commerce.pagopa.domain.scrap.entity.enums.EntityType;

public record ScrapAddRequestDto(
        Long targetId,
        EntityType targetType,
        Long userId
) {
}
