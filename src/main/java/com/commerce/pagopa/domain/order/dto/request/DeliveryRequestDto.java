package com.commerce.pagopa.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeliveryRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String recipientName,

        @NotBlank(message = "{validation.notBlank}")
        String recipientPhone,

        @NotBlank(message = "{validation.notBlank}")
        String zipcode,

        @NotBlank(message = "{validation.notBlank}")
        String address,

        @NotBlank(message = "{validation.notBlank}")
        String detailAddress,

        String deliveryRequestMemo
) {
}