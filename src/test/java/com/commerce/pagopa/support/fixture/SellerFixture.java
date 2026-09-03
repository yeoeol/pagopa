package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.user.domain.model.User;

import java.time.Instant;

public final class SellerFixture {

	public SellerFixture() {
	}

	public static Seller aSeller(User user) {
		return Seller.create(
				user,
				Instant.now()
		);
	}
}
