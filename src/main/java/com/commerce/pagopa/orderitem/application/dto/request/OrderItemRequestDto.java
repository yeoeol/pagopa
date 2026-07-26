package com.commerce.pagopa.orderitem.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 주문 항목 1건의 요청 DTO
 * "어떤 메뉴를 몇 개 주문하겠다"는 정보 한 줄을 의미합니다.
 */
public record OrderItemRequestDto(
        @NotNull(message = "{validation.notNull}")
        Long productId,

        @NotNull(message = "{validation.notNull}")
        @Min(value = 1, message = "{validation.min}")
        Integer quantity
) {
}
