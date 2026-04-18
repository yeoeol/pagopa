package com.commerce.pagopa.domain.admin.order.dto.response;

import com.commerce.pagopa.domain.order.entity.Delivery;
import com.commerce.pagopa.domain.order.entity.enums.DeliveryStatus;

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
        if (delivery == null) {
            return null;
        }
        return new DeliveryResponseDto(
                delivery.getId(),
                delivery.getRecipientName(),
                delivery.getRecipientPhone(),
                delivery.getAddress() != null ? delivery.getAddress().getZipcode() : null,
                delivery.getAddress() != null ? delivery.getAddress().getAddress() : null,
                delivery.getAddress() != null ? delivery.getAddress().getDetailAddress() : null,
                delivery.getDeliveryRequestMemo(),
                delivery.getStatus()
        );
    }
}