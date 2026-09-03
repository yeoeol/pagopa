package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;

import java.time.Instant;

public final class UserFixture {

    private UserFixture() {
    }

    public static User aUser(String suffix) {
        return User.create(
                Provider.LOCAL_TEST,
                "provider-" + suffix,
                "nick-" + suffix,
                "user-" + suffix + "@example.com",
                "http://default.img",
                Instant.now()
        );
    }
}
