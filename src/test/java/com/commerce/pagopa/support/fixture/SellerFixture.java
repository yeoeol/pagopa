package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.user.domain.model.User;

import java.time.Instant;

public final class SellerFixture {

	public SellerFixture() {
	}

	public static Seller aSeller(User user) {
		user.addUserRole(UserRoleFixture.aUserRole(user, RoleFixture.aRoleSeller()));
		return Seller.create(
				user,
				Instant.now()
		);
	}
}
