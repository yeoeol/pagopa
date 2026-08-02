package com.commerce.pagopa.delivery.domain.model.enums;

import com.commerce.pagopa.global.response.DescribedStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryStatus implements DescribedStatus {
	PREPARING("상품준비중"),
	SHIPPED("출고완료"),
	DELIVERED("배송완료"),
	;

	private final String description;
}
