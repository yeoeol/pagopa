package com.commerce.pagopa.delivery.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DeliveryRequestDto(
        @NotBlank(message = "{validation.notBlank}")
        String trackingNo,

        @NotBlank(message = "{validation.notBlank}")
        String requestMemo,

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
