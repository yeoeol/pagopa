package com.commerce.pagopa.review.application.dto.request;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReviewCreateRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String content,

        @NotNull(message = "{validation.notNull}")
        @Range(min = 1, max = 5, message = "{validation.range}")
        Integer rating,     // 1 ~ 5

        @NotNull(message = "{validation.notNull}")
        Long orderItemId,

        List<@Size(max = 512, message = "{validation.size}") String> imageUrls
) {
        public ReviewCreateRequestDto {
                if (imageUrls == null) {
                        imageUrls = List.of();
                }
        }
}
