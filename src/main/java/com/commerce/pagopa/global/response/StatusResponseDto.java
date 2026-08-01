package com.commerce.pagopa.global.response;

public record StatusResponseDto<T extends Enum<T> & DescribedStatus>(
		T status,
		String description
) {
	public static <T extends Enum<T> & DescribedStatus> StatusResponseDto<T> from(T status) {
		return new StatusResponseDto<>(
				status,
				status.getDescription()
		);
	}
}
