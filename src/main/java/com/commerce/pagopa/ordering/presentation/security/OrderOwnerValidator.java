package com.commerce.pagopa.ordering.presentation.security;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderRepository;

@Component("orderOwnerValidator")
@RequiredArgsConstructor
public class OrderOwnerValidator extends OwnerValidator<Order, Long> {

    private final OrderRepository orderRepository;

    @Override
    protected Optional<Order> findResource(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    protected Long extractOwnerId(Order order) {
        return order.getUserId();
    }
}
