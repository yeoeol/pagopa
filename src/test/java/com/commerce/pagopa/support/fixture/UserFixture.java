package com.commerce.pagopa.support.fixture;

import java.time.LocalDateTime;

import com.commerce.pagopa.identity.domain.Provider;
import com.commerce.pagopa.identity.domain.User;

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
                LocalDateTime.now()
        );
    }
}
