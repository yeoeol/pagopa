package com.commerce.pagopa.delivery.application.dto.response;

import com.commerce.pagopa.delivery.domain.model.Delivery;
import com.commerce.pagopa.delivery.domain.model.enums.DeliveryStatus;
import com.commerce.pagopa.global.entity.Address;
import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.order.application.dto.response.OrderResponseDto;

public record DeliveryResponseDto(
        Long deliveryId,
        StatusResponseDto<DeliveryStatus> status,
        String trackingNo,
        String requestMemo,
        Address address,
        OrderResponseDto order
) {
    public static DeliveryResponseDto from(Delivery delivery) {
		return new DeliveryResponseDto(
                delivery.getId(),
                StatusResponseDto.from(delivery.getStatus()),
                delivery.getTrackingNo(),
                delivery.getRequestMemo(),
                delivery.getAddress(),
                OrderResponseDto.from(delivery.getOrder())
        );
    }
}
