package com.commerce.pagopa.seller.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerStatus {
	PENDING("대기"),
	ACTIVE("정상"),
	SUSPENDED("임시정지"),
	BANNED("영구정지"),
	PAUSED("판매중지"),
	WITHDRAWN("영구탈퇴"),
	;

	private final String displayName;
}
