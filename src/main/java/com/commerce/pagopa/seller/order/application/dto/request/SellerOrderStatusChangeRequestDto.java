package com.commerce.pagopa.seller.order.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record SellerOrderStatusChangeRequestDto(
        @NotNull(message = "{validation.notNull}")
        SellerOrderStatusChange status
) {
}
