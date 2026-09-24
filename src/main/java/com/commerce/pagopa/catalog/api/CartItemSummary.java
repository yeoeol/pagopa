package com.commerce.pagopa.catalog.api;

public record CartItemSummary(
		Long cartItemId,
		Long productId,
		int quantity
) {
}
