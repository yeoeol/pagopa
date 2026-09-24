package com.commerce.pagopa.catalog.application;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.api.ProductSummary;
import com.commerce.pagopa.catalog.domain.Product;
import com.commerce.pagopa.catalog.domain.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductApiService implements ProductApi {

	private final ProductRepository productRepository;

	@Override
	public boolean existsById(Long productId) {
		return productRepository.existsById(productId);
	}

	@Override
	public Map<Long, ProductSummary> findAllByIds(Collection<Long> productIds) {
		Map<Long, ProductSummary> productSummary = new HashMap<>();

		List<Product> products = productRepository.findByIdIn(productIds);
		products.forEach(product -> productSummary.put(product.getId(), toSummary(product)));

		return productSummary;
	}

	@Override
	public ProductSummary find(Long productId) {
		Product product = productRepository.findByIdOrThrow(productId);
		return toSummary(product);
	}

	private ProductSummary toSummary(Product product) {
		return new ProductSummary(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getStatus().name(),
				product.getSellerId()
		);
	}
}
