package com.commerce.pagopa.seller.application.dto.seller.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.model.enums.SellerStatus;
import com.commerce.pagopa.seller.domain.model.enums.VerificationStatus;
import com.commerce.pagopa.user.application.dto.response.UserResponseDto;

import java.time.Instant;

public record SellerResponseDto(
		Long sellerId,
		StatusResponseDto<SellerStatus> status,
		StatusResponseDto<VerificationStatus> verificationStatus,
		Instant activatedAt,
		Instant suspendedAt,
		UserResponseDto user
) {
	public static SellerResponseDto from(Seller seller) {
		return new SellerResponseDto(
				seller.getId(),
				StatusResponseDto.from(seller.getStatus()),
				StatusResponseDto.from(seller.getVerificationStatus()),
				seller.getActivatedAt(),
				seller.getSuspendedUntil(),
				UserResponseDto.from(seller.getUser())
		);
	}
}
