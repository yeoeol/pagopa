package com.commerce.pagopa.user.application.admin.dto.request;

import java.time.Instant;

public record AdminUserSuspendRequestDto(
		Instant suspendedUntil
) {
}
