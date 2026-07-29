package com.commerce.pagopa.user.application.dto.response;

import com.commerce.pagopa.user.domain.model.enums.UserStatus;

public record UserStatusResponseDto(
		UserStatus status,
		String description
) {
	public static UserStatusResponseDto from(UserStatus status) {
		return new UserStatusResponseDto(
				status,
				status.getDescription()
		);
	}
}
