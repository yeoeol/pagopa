package com.commerce.pagopa.catalog.application.dto.request;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

public record ProductCursor(
        LocalDateTime createdAt,
        Long productId
) {
    private static final String DELIMITER = "|";

    public String encode() {
        String value = createdAt + DELIMITER + productId;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public static ProductCursor decodeNullable(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(
                    Base64.getUrlDecoder()
                            .decode(encodedCursor),
                    StandardCharsets.UTF_8
            );

            String[] values = decoded.split("\\|", -1);
            if (values.length != 2) {
                throw new IllegalArgumentException("Invalid product cursor format: " + encodedCursor);
            }

            LocalDateTime createdAt = LocalDateTime.parse(values[0]);
            long productId = Long.parseLong(values[1]);

            if (productId <= 0) {
                throw new IllegalArgumentException("Invalid product ID: " + productId);
            }

            return new ProductCursor(
                    createdAt,
                    productId
            );
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_PRODUCT_CURSOR);
        }
    }
}
