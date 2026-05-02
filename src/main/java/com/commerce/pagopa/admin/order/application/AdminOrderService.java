package com.commerce.pagopa.admin.order.application;

import com.commerce.pagopa.admin.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> findAll(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(OrderResponseDto::from);
    }
}
