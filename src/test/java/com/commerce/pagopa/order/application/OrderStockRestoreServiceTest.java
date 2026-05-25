package com.commerce.pagopa.order.application;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.model.enums.SellerOrderStatus;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.support.fixture.OrderFixture;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderStockRestoreServiceTest {

    private static final BigDecimal PRICE = new BigDecimal("10000");

    @Mock
    private ProductRepository productRepository;

    private OrderStockRestoreService orderStockRestoreService;

    @BeforeEach
    void setUp() {
        orderStockRestoreService = new OrderStockRestoreService(productRepository);
    }

    @Test
    void cancelOrderAndRestoreStock_restoresOnlyActiveSellerOrders() {
        User seller1 = UserFixture.aSeller("seller-1");
        User seller2 = UserFixture.aSeller("seller-2");
        Product product1 = productWithId(101L, seller1);
        Product product2 = productWithId(102L, seller2);

        Order order = OrderFixture.anOrder("order-1", UserFixture.aBuyer("buyer"));
        SellerOrder so1 = readySellerOrder(seller1, "order-1-1", product1);
        SellerOrder so2 = readySellerOrder(seller2, "order-1-2", product2);
        order.addSellerOrder(so1);
        order.addSellerOrder(so2);

        so1.cancelByBuyer();

        orderStockRestoreService.cancelOrderAndRestoreStock(order);

        assertThat(so1.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        assertThat(so2.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        verify(productRepository, never()).increaseStock(101L, 1);
        verify(productRepository).increaseStock(102L, 1);
    }

    @Test
    void cancelSellerOrderByBuyerAndRestoreStock_restoresSellerOrderProducts() {
        User seller = UserFixture.aSeller("seller");
        Product product = productWithId(201L, seller);
        SellerOrder sellerOrder = readySellerOrder(seller, "order-2-1", product);

        orderStockRestoreService.cancelSellerOrderByBuyerAndRestoreStock(sellerOrder);

        assertThat(sellerOrder.getStatus()).isEqualTo(SellerOrderStatus.CANCELLED);
        verify(productRepository).increaseStock(201L, 1);
    }

    private Product productWithId(Long id, User seller) {
        Product product = ProductFixture.aProduct(null, seller, PRICE);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private SellerOrder readySellerOrder(User seller, String sellerOrderNumber, Product product) {
        SellerOrder sellerOrder = SellerOrderFixture.aSellerOrder(seller, sellerOrderNumber);
        sellerOrder.addOrderProduct(OrderProductFixture.anOrderProduct(product));
        sellerOrder.pay();
        return sellerOrder;
    }
}
