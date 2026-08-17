package com.commerce.pagopa.role.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleCode {
	ROLE_USER("회원"),
	ROLE_SELLER("판매자"),
	ROLE_ADMIN("관리자"),
	;

	private final String description;
}
