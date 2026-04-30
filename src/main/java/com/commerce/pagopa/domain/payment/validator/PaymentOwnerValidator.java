package com.commerce.pagopa.domain.payment.validator;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.user.entity.User;
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
