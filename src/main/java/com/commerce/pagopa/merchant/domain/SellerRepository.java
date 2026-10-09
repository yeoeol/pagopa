package com.commerce.pagopa.merchant.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.SELLER_NOT_FOUND;

public interface SellerRepository {

    Seller save(Seller seller);

    Optional<Seller> findById(Long id);

    Optional<Seller> findByUserId(Long userId);

    Page<Seller> findPendingRequests(SellerStatus sellerStatus, Pageable pageable);

    default Seller findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(SELLER_NOT_FOUND));
    }

    default Seller findByUserIdOrThrow(Long userId) {
        return findByUserId(userId).orElseThrow(() -> new BusinessException(SELLER_NOT_FOUND));
    }
}
