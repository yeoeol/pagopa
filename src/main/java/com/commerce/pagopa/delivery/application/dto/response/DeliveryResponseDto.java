package com.commerce.pagopa.delivery.application.dto.response;

import com.commerce.pagopa.delivery.domain.model.Address;
import com.commerce.pagopa.delivery.domain.model.Delivery;

public record DeliveryResponseDto(
        Long deliveryId,
        String recipientName,
        String recipientPhone,
        String zipcode,
        String address,
        String detailAddress,
        String deliveryRequestMemo
) {
    public static DeliveryResponseDto from(Delivery delivery) {
        Address address = delivery.getAddress();
        return new DeliveryResponseDto(
                delivery.getId(),
                delivery.getRecipientName(),
                delivery.getRecipientPhone(),
                address != null ? address.getZipcode() : null,
                address != null ? address.getAddress() : null,
                address != null ? address.getDetailAddress() : null,
                delivery.getRequestMemo()
        );
    }
}
