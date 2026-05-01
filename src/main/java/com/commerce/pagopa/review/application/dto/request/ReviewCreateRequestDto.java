package com.commerce.pagopa.review.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import java.util.List;

public record ReviewCreateRequestDto(
        @NotNull(message = "{validation.notNull}")
        @Range(min = 1, max = 5, message = "{validation.range}")
        Integer rating,     // 1 ~ 5

        @NotBlank(message = "{validation.notBlank}")
        String content,

        @NotNull(message = "{validation.notNull}")
        Long orderProductId,

        @NotEmpty(message = "{validation.notEmpty}")
        List<@NotBlank(message = "{validation.notBlank}")
             @Size(max = 512, message = "{validation.size}") String> imageUrls
) {
}
