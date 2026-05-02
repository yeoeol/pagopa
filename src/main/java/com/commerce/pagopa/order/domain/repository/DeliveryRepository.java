package com.commerce.pagopa.order.domain.repository;

import com.commerce.pagopa.order.domain.model.Delivery;

public interface DeliveryRepository {

    Delivery save(Delivery delivery);
}
