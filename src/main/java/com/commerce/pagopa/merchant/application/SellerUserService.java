package com.commerce.pagopa.merchant.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.api.UserValidateApi;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerRepository;

@Service
@RequiredArgsConstructor
public class SellerUserService {

    private final SellerRepository sellerRepository;
    private final UserValidateApi userValidateApi;

    @Transactional
    public void request(Long userId) {
        userValidateApi.validateRequestable(userId);

        LocalDateTime requestedAt = LocalDateTime.now();

        Seller seller = sellerRepository.findByUserId(userId)
                .map(existingSeller -> {
                    existingSeller.requestAgain(requestedAt);
                    return existingSeller;
                })
                .orElseGet(
                        () -> Seller.create(
                                userId,
                                requestedAt
                        )
                );

        sellerRepository.save(seller);
    }
}
