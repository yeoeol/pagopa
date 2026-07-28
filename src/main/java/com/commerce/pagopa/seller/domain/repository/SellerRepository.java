package com.commerce.pagopa.seller.domain.repository;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.seller.domain.model.Seller;

import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.SELLER_NOT_FOUND;

public interface SellerRepository {

	Optional<Seller> findById(Long id);

	Optional<Seller> findByUserId(Long userId);

	default Seller findByIdOrThrow(Long id) {
		return findById(id).orElseThrow(() -> new BusinessException(SELLER_NOT_FOUND));
	}

	default Seller findByUserIdOrThrow(Long userId) {
		return findByUserId(userId).orElseThrow(() -> new BusinessException(SELLER_NOT_FOUND));
	}
}
