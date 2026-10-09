package com.commerce.pagopa.catalog.application.dto.request;

import jakarta.validation.constraints.Size;

public record ProductSearchCondition(
        @Size(
                min = 1,
                max = 100,
                message = "{validation.size}"
        ) String productName // 상품명
                             // 검색
) {
}
