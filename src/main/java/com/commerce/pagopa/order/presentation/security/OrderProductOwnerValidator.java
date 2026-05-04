package com.commerce.pagopa.order.presentation.security;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.repository.OrderProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("orderProductOwnerValidator")
@RequiredArgsConstructor
public class OrderProductOwnerValidator extends OwnerValidator<OrderProduct, Long> {

    private final OrderProductRepository orderProductRepository;

    @Override
    protected Optional<OrderProduct> findResource(Long orderProductId) {
        return orderProductRepository.findByIdWithOrderAndUser(orderProductId);
    }

    @Override
    protected Long extractOwnerId(OrderProduct orderProduct) {
        if (orderProduct.getSellerOrder() == null) {
            return null;
        }
        Order order = orderProduct.getSellerOrder().getOrder();
        if (order == null) {
            return null;
        }
        return Optional.ofNullable(order.getUser())
                .map(User::getId)
                .orElse(null);
    }
}
