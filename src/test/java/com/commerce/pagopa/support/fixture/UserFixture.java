package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;

import java.util.UUID;

public final class UserFixture {

    private UserFixture() {
    }

    public static User aBuyer() {
        return aUser(Role.ROLE_USER, UUID.randomUUID().toString());
    }

    public static User aBuyer(String suffix) {
        return aUser(Role.ROLE_USER, suffix);
    }

    public static User aSeller() {
        return aUser(Role.ROLE_SELLER, UUID.randomUUID().toString());
    }

    public static User aSeller(String suffix) {
        return aUser(Role.ROLE_SELLER, suffix);
    }

    private static User aUser(Role role, String suffix) {
        return User.create(
                "user-" + suffix + "@example.com",
                "nick-" + suffix,
                null,
                Provider.NAVER,
                "provider-" + suffix,
                role
        );
    }
}
