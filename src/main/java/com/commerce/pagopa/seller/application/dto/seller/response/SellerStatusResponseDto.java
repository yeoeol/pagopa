package com.commerce.pagopa.seller.application.dto.seller.response;

import com.commerce.pagopa.seller.domain.model.enums.SellerStatus;

public record SellerStatusResponseDto(
		SellerStatus status,
		String description
) {
	public static SellerStatusResponseDto from(SellerStatus status) {
		return new SellerStatusResponseDto(
				status,
				status.getDisplayName()
		);
	}
}
