package com.commerce.pagopa.domain.seller.order.validator;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("sellerOrderOwnerValidator")
@RequiredArgsConstructor
public class SellerOrderOwnerValidator {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(Long orderId, Long userId) {
        if (orderId == null || userId == null) {
            return false;
        }

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        User seller = order.getOrderProducts().getFirst().getProduct().getSeller();
        if (seller == null) {
            return false;
        }
        return seller.getId().equals(userId);
    }
}
