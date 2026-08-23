package com.commerce.pagopa.seller.application.admin.dto.response;

public record AdminSellerRejectResponseDto(
		String reason
) {
	public static AdminSellerRejectResponseDto from(String reason) {
		return new AdminSellerRejectResponseDto(
				reason
		);
	}
}
