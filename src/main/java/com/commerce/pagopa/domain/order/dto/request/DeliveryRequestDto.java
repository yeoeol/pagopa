package com.commerce.pagopa.domain.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeliveryRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String recipientName,

        @NotBlank(message = "{validation.notBlank}")
        @Pattern(regexp = "^[0-9]{9,15}$")
        String recipientPhone,

        @NotBlank(message = "{validation.notBlank}")
        @Pattern(regexp = "^[0-9]{5,6}$")
        String zipcode,

        @NotBlank(message = "{validation.notBlank}")
        String address,

        @NotBlank(message = "{validation.notBlank}")
        String detailAddress,

        String deliveryRequestMemo
) {
}