package com.commerce.pagopa.catalog.application;

import com.commerce.pagopa.catalog.api.ProductApi;
import com.commerce.pagopa.catalog.domain.ProductRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductApiService implements ProductApi {

	private final ProductRepository productRepository;

	@Override
	public boolean existsById(Long productId) {
		return productRepository.existsById(productId);
	}
}
