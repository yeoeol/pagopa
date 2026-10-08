package com.commerce.pagopa.catalog.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.ProductStockApi;
import com.commerce.pagopa.catalog.api.ProductStockRequest;
import com.commerce.pagopa.catalog.api.ProductStockResult;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductStockApiService implements ProductStockApi {

    private final ProductRepository productRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<ProductStockResult> decreaseStocks(List<ProductStockRequest> requests) {
        Map<Long, Integer> totalQuantityByProductId = requests.stream()
                .collect(Collectors.toMap(ProductStockRequest::productId, ProductStockRequest::quantity, Integer::sum));

        // 데드락 방지
        List<Long> productIds = totalQuantityByProductId.keySet()
                .stream()
                .sorted()
                .toList();

        List<ProductStockResult> results = new ArrayList<>();

        for (Long productId : productIds) {
            Product product = productRepository.findByIdForUpdateOrThrow(productId);

            product.decreaseStock(totalQuantityByProductId.get(productId));

            results.add(new ProductStockResult(product.getId(), product.getName(), product.getPrice(),
                    totalQuantityByProductId.get(productId)));
        }

        return results;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public List<ProductStockResult> restoreStocks(List<ProductStockRequest> requests) {
        // 데드락 방지
        List<Long> productIds = requests.stream()
                .map(ProductStockRequest::productId)
                .distinct()
                .sorted()
                .toList();

        Map<Long, Product> productMap = new HashMap<>();

        for (Long productId : productIds) {
            Product product = productRepository.findByIdForUpdateOrThrow(productId);
            productMap.put(productId, product);
        }

        List<ProductStockResult> results = new ArrayList<>();

        // 주문 항목 수량만큼 재고 복구
        for (ProductStockRequest request : requests) {
            Product product = productMap.get(request.productId());
            product.increaseStock(request.quantity());

            results.add(
                    new ProductStockResult(product.getId(), product.getName(), product.getPrice(), request.quantity()));
        }

        return results;
    }
}
