package com.commerce.pagopa.seller.domain.model.enums;

import com.commerce.pagopa.global.response.DescribedStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerStatus implements DescribedStatus {
	PENDING("대기"),
	ACTIVE("정상"),
	SUSPENDED("임시정지"),
	BANNED("영구정지"),
	PAUSED("판매중지"),
	WITHDRAWN("영구탈퇴"),
	;

	private final String description;
}
