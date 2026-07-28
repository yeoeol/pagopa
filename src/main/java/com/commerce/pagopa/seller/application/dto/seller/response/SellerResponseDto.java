package com.commerce.pagopa.seller.application.dto.seller.response;

import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.model.enums.VerificationStatus;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.time.Instant;

public record SellerResponseDto(
		Long sellerId,
		SellerStatusResponseDto sellerStatus,
		VerificationStatus verificationStatus,
		Instant activatedAt,
		Instant suspendedAt,
		UserResponseDto user
) {
	public static SellerResponseDto from(Seller seller) {
		return new SellerResponseDto(
				seller.getId(),
				SellerStatusResponseDto.from(seller.getStatus()),
				seller.getVerificationStatus(),
				seller.getActivatedAt(),
				seller.getSuspendedAt(),
				UserResponseDto.from(seller.getUser())
		);
	}
}
