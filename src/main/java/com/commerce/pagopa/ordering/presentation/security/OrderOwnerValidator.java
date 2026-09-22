package com.commerce.pagopa.ordering.presentation.security;

import com.commerce.pagopa.global.validator.OwnerValidator;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.ordering.domain.order.Order;
import com.commerce.pagopa.ordering.domain.order.OrderRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

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
        return Optional.ofNullable(order.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
