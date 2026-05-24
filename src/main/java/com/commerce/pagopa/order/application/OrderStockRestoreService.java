package com.commerce.pagopa.order.application;

import com.commerce.pagopa.order.domain.model.Order;
import com.commerce.pagopa.order.domain.model.OrderProduct;
import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderStockRestoreService {

    private final ProductRepository productRepository;

    public void cancelOrderAndRestoreStock(Order order) {
        List<OrderProduct> stockRestoreTargets = activeStockRestoreTargets(order);

        order.cancel();
        restoreStock(stockRestoreTargets);
    }

    public void cancelSellerOrderAndRestoreStock(SellerOrder sellerOrder) {
        List<OrderProduct> stockRestoreTargets = sellerOrder.getOrderProducts();

        sellerOrder.cancel();
        restoreStock(stockRestoreTargets);
    }

    public void cancelSellerOrderByBuyerAndRestoreStock(SellerOrder sellerOrder) {
        List<OrderProduct> stockRestoreTargets = sellerOrder.getOrderProducts();

        sellerOrder.cancelByBuyer();
        restoreStock(stockRestoreTargets);
    }

    private List<OrderProduct> activeStockRestoreTargets(Order order) {
        return order.getSellerOrders().stream()
                .filter(sellerOrder -> !sellerOrder.isCancelled())
                .flatMap(sellerOrder -> sellerOrder.getOrderProducts().stream())
                .toList();
    }

    private void restoreStock(List<OrderProduct> orderProducts) {
        orderProducts.forEach(orderProduct -> productRepository.increaseStock(
                orderProduct.getProduct().getId(),
                orderProduct.getQuantity()
        ));
    }
}
