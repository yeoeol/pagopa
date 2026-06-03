package com.commerce.pagopa.seller.order.application;

import com.commerce.pagopa.order.application.OrderStockRestoreService;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.repository.SellerOrderRepository;
import com.commerce.pagopa.seller.order.application.dto.request.SellerOrderStatusChangeRequestDto;
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
    private final OrderStockRestoreService orderStockRestoreService;

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
    public void changeStatus(Long sellerOrderId, Long sellerId, SellerOrderStatusChangeRequestDto requestDto) {
        SellerOrder sellerOrder = sellerOrderRepository.getByIdAndSellerIdOrThrow(sellerOrderId, sellerId);

        switch (requestDto.status()) {
            case DELIVERING -> sellerOrder.deliver();
            case COMPLETED -> sellerOrder.complete();
            case CANCELLED -> orderStockRestoreService.cancelSellerOrderAndRestoreStock(sellerOrder);
        }
    }
}
