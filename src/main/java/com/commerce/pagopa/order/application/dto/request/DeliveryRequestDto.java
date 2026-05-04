package com.commerce.pagopa.order.application.dto.request;

import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;

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
    public Delivery toDelivery() {
        Address addr = new Address(zipcode, address, detailAddress);
        return Delivery.create(addr, recipientName, recipientPhone, deliveryRequestMemo);
    }
}
