package com.commerce.pagopa.delivery.application.dto.response;

import com.commerce.pagopa.delivery.domain.model.Address;
import com.commerce.pagopa.delivery.domain.model.Delivery;

public record DeliveryResponseDto(
        Long deliveryId,
        String status,
        String trackingNo,
        String requestMemo,
        String zipcode,
        String address,
        String detailAddress
) {
    public static DeliveryResponseDto from(Delivery delivery) {
        Address address = delivery.getAddress();
        return new DeliveryResponseDto(
                delivery.getId(),
                delivery.getStatus().name(),
                delivery.getTrackingNo(),
                delivery.getRequestMemo(),
                address != null ? address.getZipcode() : null,
                address != null ? address.getAddress() : null,
                address != null ? address.getDetailAddress() : null
        );
    }
}
