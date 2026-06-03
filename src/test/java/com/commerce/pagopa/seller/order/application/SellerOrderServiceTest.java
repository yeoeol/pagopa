package com.commerce.pagopa.seller.order.application;

import com.commerce.pagopa.order.application.OrderStockRestoreService;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.order.domain.repository.SellerOrderRepository;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.seller.order.application.dto.request.SellerOrderStatusChange;
import com.commerce.pagopa.seller.order.application.dto.request.SellerOrderStatusChangeRequestDto;
import com.commerce.pagopa.support.fixture.OrderProductFixture;
import com.commerce.pagopa.support.fixture.ProductFixture;
import com.commerce.pagopa.support.fixture.SellerOrderFixture;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SellerOrderServiceTest {

    @Mock
    private SellerOrderRepository sellerOrderRepository;

    @Mock
    private OrderStockRestoreService orderStockRestoreService;

    private SellerOrderService sellerOrderService;

    @BeforeEach
    void setUp() {
        sellerOrderService = new SellerOrderService(sellerOrderRepository, orderStockRestoreService);
    }

    @Test
    void changeStatus_delivering_deliversSellerOrder() {
        SellerOrder sellerOrder = newReadySellerOrder();
        when(sellerOrderRepository.getByIdAndSellerIdOrThrow(1L, 2L)).thenReturn(sellerOrder);

        sellerOrderService.changeStatus(
                1L,
                2L,
                new SellerOrderStatusChangeRequestDto(SellerOrderStatusChange.DELIVERING)
        );

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.DELIVERING);
        verify(orderStockRestoreService, never()).cancelSellerOrderAndRestoreStock(any());
    }

    @Test
    void changeStatus_completed_completesSellerOrder() {
        SellerOrder sellerOrder = newReadySellerOrder();
        sellerOrder.deliver();
        when(sellerOrderRepository.getByIdAndSellerIdOrThrow(1L, 2L)).thenReturn(sellerOrder);

        sellerOrderService.changeStatus(
                1L,
                2L,
                new SellerOrderStatusChangeRequestDto(SellerOrderStatusChange.COMPLETED)
        );

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.COMPLETED);
        verify(orderStockRestoreService, never()).cancelSellerOrderAndRestoreStock(any());
    }

    @Test
    void changeStatus_cancelled_delegatesStockRestoreCancel() {
        SellerOrder sellerOrder = newReadySellerOrder();
        when(sellerOrderRepository.getByIdAndSellerIdOrThrow(1L, 2L)).thenReturn(sellerOrder);

        sellerOrderService.changeStatus(
                1L,
                2L,
                new SellerOrderStatusChangeRequestDto(SellerOrderStatusChange.CANCELLED)
        );

        verify(orderStockRestoreService).cancelSellerOrderAndRestoreStock(sellerOrder);
    }

    private SellerOrder newReadySellerOrder() {
        User seller = UserFixture.aSeller("seller");
        Product product = ProductFixture.aProduct(null, seller, new BigDecimal("10000"));
        SellerOrder sellerOrder = SellerOrderFixture.aSellerOrder(seller, "ORD20260502-001-1");
        sellerOrder.addOrderProduct(OrderProductFixture.anOrderProduct(product));
        sellerOrder.pay();
        return sellerOrder;
    }
}
