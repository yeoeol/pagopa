package com.commerce.pagopa.basket.api;

public record CartItemSummary(Long cartItemId, Long productId, int quantity) {
}
