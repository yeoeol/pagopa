package com.commerce.pagopa.seller.application.dto.seller.response;

import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.model.enums.SellerStatus;
import com.commerce.pagopa.seller.domain.model.enums.VerificationStatus;
import com.commerce.pagopa.user.domain.model.User;

import java.time.Instant;

public record SellerResponseDto(
		Long sellerId,
		SellerStatus status,
		VerificationStatus verificationStatus,
		Instant activatedAt,
		Instant suspendedAt,
		User user
) {
	public static SellerResponseDto from(Seller seller) {
		return new SellerResponseDto(
				seller.getId(),
				seller.getStatus(),
				seller.getVerificationStatus(),
				seller.getActivatedAt(),
				seller.getSuspendedAt(),
				seller.getUser()
		);
	}
}
