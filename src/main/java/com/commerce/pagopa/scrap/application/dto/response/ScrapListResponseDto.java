package com.commerce.pagopa.scrap.application.dto.response;

import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.util.List;
import java.util.Map;

public record ScrapListResponseDto(
        Map<String, Long> count,
        UserResponseDto user,
        List<ScrapCollectionItem> collection
) {
    public static ScrapListResponseDto of(
            Map<String, Long> count,
            UserResponseDto user,
            List<ScrapCollectionItem> collection
    ) {
        return new ScrapListResponseDto(
                count,
                user,
                collection
        );
    }
}
