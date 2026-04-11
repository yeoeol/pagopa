package com.commerce.pagopa.domain.order.dto.request;

import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CartOrderRequestDto(
        @NotNull(message = "{validation.notNull}")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "{validation.notNull}")
        List<Long> cartIds
) {
}
