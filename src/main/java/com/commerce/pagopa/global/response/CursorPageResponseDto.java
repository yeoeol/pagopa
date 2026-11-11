package com.commerce.pagopa.global.response;

import java.util.List;

public record CursorPageResponseDto<T>(
        List<T> content,
        String nextCursor,
        boolean hasNext
) {
    public static <T> CursorPageResponseDto<T> from(
            List<T> content,
            String nextCursor,
            boolean hasNext
    ) {
        return new CursorPageResponseDto<>(
                content,
                nextCursor,
                hasNext
        );
    }
}
