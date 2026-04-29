package com.commerce.pagopa.domain.order.validator;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.OrderProduct;
import com.commerce.pagopa.domain.order.repository.OrderProductRepository;
import com.commerce.pagopa.global.validator.OwnerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("orderProductOwnerValidator")
@RequiredArgsConstructor
public class OrderProductOwnerValidator extends OwnerValidator<OrderProduct> {

    private final OrderProductRepository orderProductRepository;

    @Override
    protected Optional<OrderProduct> findResource(Long orderProductId) {
        return orderProductRepository.findByIdWithOrderAndUser(orderProductId);
    }

    @Override
    protected Long extractOwnerId(OrderProduct orderProduct) {
        Order order = orderProduct.getOrder();
        if (order == null || order.getUser() == null) {
            return null;
        }
        return order.getUser().getId();
    }
}
