package com.commerce.pagopa.seller.order.presentation.security;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("sellerOrderOwnerValidator")
@RequiredArgsConstructor
public class SellerOrderOwnerValidator extends OwnerValidator<Order, Long> {

    private final OrderRepository orderRepository;

    @Override
    protected Optional<Order> findResource(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    protected Long extractOwnerId(Order order) {
        if (order.getOrderProducts().isEmpty()) {
            return null;
        }
        User seller = order.getOrderProducts().getFirst().getProduct().getSeller();
        return Optional.ofNullable(seller)
                .map(User::getId)
                .orElse(null);
    }
}
