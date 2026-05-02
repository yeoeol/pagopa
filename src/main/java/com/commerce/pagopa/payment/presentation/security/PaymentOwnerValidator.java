package com.commerce.pagopa.payment.presentation.security;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("paymentOwnerValidator")
@RequiredArgsConstructor
public class PaymentOwnerValidator extends OwnerValidator<Order, String> {

    private final OrderRepository orderRepository;

    @Override
    protected Optional<Order> findResource(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    protected Long extractOwnerId(Order order) {
        return Optional.ofNullable(order.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
