package com.commerce.pagopa.catalog.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ProductCursorRequestDto(
        String cursor,
        @Min(value = 1, message = "{validation.min}")
        @Max(value = 100, message = "{validation.max}")
        Integer size
) {
    private static final int DEFAULT_SIZE = 20;

    public int resolvedSize() {
        return size == null ? DEFAULT_SIZE : size;
    }
}
