package com.commerce.pagopa.domain.seller.order.validator;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("sellerOrderOwnerValidator")
@RequiredArgsConstructor
public class SellerOrderOwnerValidator extends OwnerValidator<Order> {

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
        return seller == null ? null : seller.getId();
    }
}
