package com.commerce.pagopa.ordering.application;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.ProductStockApi;
import com.commerce.pagopa.catalog.api.ProductStockRequest;
import com.commerce.pagopa.catalog.api.ProductStockResult;
import com.commerce.pagopa.ordering.domain.order.Order;

@Component
@RequiredArgsConstructor
public class OrderStockRestorer {

    private final ProductStockApi productStockApi;

    public List<ProductStockResult> restore(Order order) {
        return productStockApi.restoreStocks(
                order.getOrderItems()
                        .stream()
                        .map(orderItem -> new ProductStockRequest(
                                orderItem.getProductId(),
                                orderItem.getOrderQuantity()
                        ))
                        .toList()
        );
    }
}
