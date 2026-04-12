package com.commerce.pagopa.domain.scrap.dto.response;

import com.commerce.pagopa.domain.scrap.entity.Scrap;
import com.commerce.pagopa.domain.user.dto.response.UserResponseDto;

public record ScrapResponseDto(
        Long scrapId,
        Long targetId,
        String targetType,
        UserResponseDto user
) {
    public static ScrapResponseDto from(Scrap scrap) {
        return new ScrapResponseDto(
                scrap.getId(),
                scrap.getTargetId(),
                scrap.getTargetType().getDescription(),
                UserResponseDto.from(scrap.getUser())
        );
    }
}
