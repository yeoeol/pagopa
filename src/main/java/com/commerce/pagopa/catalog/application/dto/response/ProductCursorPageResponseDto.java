package com.commerce.pagopa.catalog.application.dto.response;

import java.util.List;

public record ProductCursorPageResponseDto(
        List<ProductResponseDto> content,
        String nextCursor,
        boolean hasNext
) {
}
