package com.commerce.pagopa.support.fixture;

import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.user.domain.model.User;

public final class SellerOrderFixture {

    private SellerOrderFixture() {
    }

    public static SellerOrder aSellerOrder(User seller) {
        return aSellerOrder(seller, "ORD-1-1");
    }

    public static SellerOrder aSellerOrder(User seller, String sellerOrderNumber) {
        return SellerOrder.create(seller, sellerOrderNumber);
    }
}
