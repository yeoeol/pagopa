package com.commerce.pagopa.seller.domain.model.enums;

import com.commerce.pagopa.global.response.DescribedStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationStatus implements DescribedStatus {
	UNVERIFIED("인증되지 않음"),
	VERIFIED("인증됨"),
	;

	private final String description;
}
