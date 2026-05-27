package com.commerce.pagopa.scrap.application.dto.response;

import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.util.List;
import java.util.Map;

public record ScrapListResponseDto(
        Map<String, Integer> count,
        UserResponseDto user,
        List<ScrapCollectionItem> collection
) {
}
