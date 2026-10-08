package com.commerce.pagopa.merchant.application.admin;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.api.UserApi;
import com.commerce.pagopa.identity.api.UserSummary;
import com.commerce.pagopa.merchant.application.admin.dto.response.AdminSellerPageResponseDto;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerRepository;
import com.commerce.pagopa.merchant.domain.SellerStatus;

@Service
@RequiredArgsConstructor
public class AdminSellerService {

    private final SellerRepository sellerRepository;
    private final UserApi userApi;

    @Transactional(readOnly = true)
    public AdminSellerPageResponseDto getPendingSellers(Pageable pageable) {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "statusChangedAt")
                        .and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Seller> sellers = sellerRepository.findPendingRequests(SellerStatus.PENDING, pageRequest);

        Map<Long, UserSummary> summary = userApi.findAllByIdIn(sellers.stream()
                .map(Seller::getUserId)
                .toList());
        return AdminSellerPageResponseDto.from(sellers, summary);
    }

    @Transactional
    public void approve(Long sellerId) {
        Seller seller = sellerRepository.findByIdOrThrow(sellerId);

        userApi.grantSellerRole(seller.getUserId());
        seller.activate(LocalDateTime.now());
    }

    @Transactional
    public void reject(Long sellerId) {
        Seller seller = sellerRepository.findByIdOrThrow(sellerId);
        seller.reject(LocalDateTime.now());
    }
}
