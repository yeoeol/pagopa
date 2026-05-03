package com.commerce.pagopa.seller.order.application;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.OrderStatus;
import com.commerce.pagopa.order.domain.repository.OrderRepository;
import com.commerce.pagopa.order.domain.repository.SellerOrderRepository;
import com.commerce.pagopa.seller.order.application.dto.request.OrderStatusChangeRequestDto;
import com.commerce.pagopa.seller.order.application.dto.response.OrderResponseDto;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;

    /**
     * 셀러의 주문(=SellerOrder) 목록 페이징 조회.
     * NOTE: PR #3에서는 OrderRepository.findAllBySellerId 경유 후 SellerOrder를 매칭한다.
     * PR #4에서 SellerOrderRepository로 직접 페이징 조회로 단순화 예정.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> findAll(Long sellerId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllBySellerId(sellerId, pageable);
        return orderPage.map(order -> {
            SellerOrder so = sellerOrderRepository.getBySellerIdAndOrderIdOrThrow(sellerId, order.getId());
            return OrderResponseDto.from(so);
        });
    }

    @Transactional(readOnly = true)
    public OrderResponseDto find(Long orderId, Long sellerId) {
        SellerOrder so = sellerOrderRepository.getBySellerIdAndOrderIdOrThrow(sellerId, orderId);
        return OrderResponseDto.from(so);
    }

    /**
     * 셀러가 자기 SellerOrder의 상태를 변경한다.
     * 입력은 기존 호환을 위해 OrderStatus를 사용하지만, PAID는 셀러가 변경할 수 없다(결제 단계는 PaymentService 소관)
     */
    @Transactional
    public void changeStatus(Long orderId, Long sellerId, OrderStatusChangeRequestDto requestDto) {
        SellerOrder so = sellerOrderRepository.getBySellerIdAndOrderIdOrThrow(sellerId, orderId);
        OrderStatus status = requestDto.status();

        switch (status) {
            case DELIVERING -> so.deliver();
            case COMPLETED -> so.complete();
            case CANCELLED -> so.cancel();
            case PAID, ORDERED -> throw new BusinessException(ErrorCode.ORDER_CANNOT_DELIVER, "셀러가 변경할 수 없는 상태입니다: " + status);
        }
    }
}
