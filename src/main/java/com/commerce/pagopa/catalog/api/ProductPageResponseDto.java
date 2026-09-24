package com.commerce.pagopa.catalog.api;

import java.util.List;

public record ProductPageResponseDto(
		List<ProductSummary> content,
		int page,
		int size,
		long totalElements,
		int totalPages,
		boolean first,
		boolean last,
		int startPage,
		int endPage
) {
}
