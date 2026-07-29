package com.commerce.pagopa.seller.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationStatus {
	UNVERIFIED("인증되지 않음"),
	VERIFIED("인증됨"),
	;

	private final String description;
}
