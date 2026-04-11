package com.commerce.pagopa.domain.order.dto.request;

import com.commerce.pagopa.domain.order.entity.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequestDto(
        @NotNull(message = "{validation.notNull}")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "{validation.notEmpty}")
        List<@Valid OrderProductRequestDto> products
) {
}
