package com.commerce.pagopa.global.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record OffsetPageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> OffsetPageResponseDto<T> from(Page<T> page) {
        return new OffsetPageResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
