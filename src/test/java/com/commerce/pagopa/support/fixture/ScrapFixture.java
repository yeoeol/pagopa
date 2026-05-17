package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.scrap.domain.model.EntityType;
import com.commerce.pagopa.scrap.domain.model.Scrap;
import com.commerce.pagopa.user.domain.model.User;

public final class ScrapFixture {

    private ScrapFixture() {
    }

    public static Scrap aProductScrap(User user, Long productId) {
        return Scrap.create(productId, EntityType.PRODUCT, user);
    }
}
