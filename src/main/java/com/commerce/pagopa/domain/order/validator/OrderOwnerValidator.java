package com.commerce.pagopa.domain.order.validator;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
