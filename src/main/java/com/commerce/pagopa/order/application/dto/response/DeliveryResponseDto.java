package com.commerce.pagopa.order.application.dto.response;

import com.commerce.pagopa.order.domain.model.Address;
import com.commerce.pagopa.order.domain.model.Delivery;
import com.commerce.pagopa.order.domain.model.enums.DeliveryStatus;

public record DeliveryResponseDto(
        Long deliveryId,
        String recipientName,
        String recipientPhone,
        String zipcode,
        String address,
        String detailAddress,
        String deliveryRequestMemo,
        DeliveryStatus status
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
                delivery.getDeliveryRequestMemo(),
                delivery.getStatus()
        );
    }
}
