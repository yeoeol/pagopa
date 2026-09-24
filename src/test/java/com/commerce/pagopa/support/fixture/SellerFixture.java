package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.merchant.domain.Seller;

import java.time.LocalDateTime;

public final class SellerFixture {

	public SellerFixture() {
	}

	public static Seller aSeller(Long userId) {
		return Seller.create(
				userId,
				LocalDateTime.now()
		);
	}
}
