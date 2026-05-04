package com.commerce.pagopa.seller.order.application;

import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.repository.SellerOrderRepository;
import com.commerce.pagopa.seller.order.application.dto.request.OrderStatusChangeRequestDto;
import com.commerce.pagopa.seller.order.application.dto.response.SellerOrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final SellerOrderRepository sellerOrderRepository;

    @Transactional(readOnly = true)
    public Page<SellerOrderResponseDto> findAll(Long sellerId, Pageable pageable) {
        return sellerOrderRepository.findBySellerId(sellerId, pageable)
                .map(SellerOrderResponseDto::from);
    }

    @Transactional(readOnly = true)
    public SellerOrderResponseDto find(Long sellerOrderId, Long sellerId) {
        SellerOrder sellerOrder = sellerOrderRepository.getByIdAndSellerIdOrThrow(sellerOrderId, sellerId);
        return SellerOrderResponseDto.from(sellerOrder);
    }

    @Transactional
    public void changeStatus(Long sellerOrderId, Long sellerId, OrderStatusChangeRequestDto requestDto) {
        SellerOrder sellerOrder = sellerOrderRepository.getByIdAndSellerIdOrThrow(sellerOrderId, sellerId);
        sellerOrder.changeStatus(requestDto.status());
    }
}
