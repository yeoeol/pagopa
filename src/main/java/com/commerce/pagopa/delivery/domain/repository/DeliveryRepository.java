package com.commerce.pagopa.delivery.domain.repository;

import com.commerce.pagopa.delivery.domain.model.Delivery;

public interface DeliveryRepository {

    Delivery save(Delivery delivery);
}
