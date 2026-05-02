package com.commerce.pagopa.scrap.application.dto.response;

import com.commerce.pagopa.user.application.dto.response.UserResponseDto;
import com.commerce.pagopa.scrap.domain.model.Scrap;

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
