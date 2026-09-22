package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.merchant.domain.Seller;

import java.time.LocalDateTime;

public final class SellerFixture {

	public SellerFixture() {
	}

	public static Seller aSeller(User user) {
		return Seller.create(
				user,
				LocalDateTime.now()
		);
	}
}
