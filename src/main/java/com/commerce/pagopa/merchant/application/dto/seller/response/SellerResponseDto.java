package com.commerce.pagopa.merchant.application.dto.seller.response;

import java.time.LocalDateTime;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerStatus;
import com.commerce.pagopa.merchant.domain.VerificationStatus;

public record SellerResponseDto(
        Long sellerId,
        StatusResponseDto<SellerStatus> status,
        StatusResponseDto<VerificationStatus> verificationStatus,
        LocalDateTime statusChangedAt,
        Long userId
) {
    public static SellerResponseDto from(Seller seller) {
        return new SellerResponseDto(
                seller.getId(),
                StatusResponseDto.from(seller.getStatus()),
                StatusResponseDto.from(seller.getVerificationStatus()),
                seller.getStatusChangedAt(),
                seller.getUserId()
        );
    }
}
