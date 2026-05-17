package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.user.domain.model.User;

import java.util.UUID;

public final class SellerOrderFixture {

    private SellerOrderFixture() {
    }

    public static SellerOrder aSellerOrder(User seller) {
        return aSellerOrder(seller, "ORD-1-" + UUID.randomUUID());
    }

    public static SellerOrder aSellerOrder(User seller, String sellerOrderNumber) {
        return SellerOrder.create(seller, sellerOrderNumber);
    }
}
