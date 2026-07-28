package com.commerce.pagopa.seller.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SellerStatus {
	PENDING("대기중"),
	ACTIVE("활성화"),
	SUSPENDED("임시정지"),
	BANNED("영구정지"),
	;

	private final String displayName;
}
