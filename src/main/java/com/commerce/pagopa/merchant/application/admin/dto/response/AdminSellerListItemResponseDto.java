package com.commerce.pagopa.merchant.application.admin.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerStatus;
import com.commerce.pagopa.merchant.domain.VerificationStatus;

import java.time.LocalDateTime;

public record AdminSellerListItemResponseDto(
        Long sellerId,
        Long userId,
        String name,
        String email,
        StatusResponseDto<SellerStatus> status,
        StatusResponseDto<VerificationStatus> verificationStatus,
        LocalDateTime requestedAt
) {
    public static AdminSellerListItemResponseDto from(Seller seller) {
        return new AdminSellerListItemResponseDto(
                seller.getId(),
                seller.getUser().getId(),
                seller.getUser().getName(),
                seller.getUser().getEmail(),
                StatusResponseDto.from(seller.getStatus()),
                StatusResponseDto.from(seller.getVerificationStatus()),
                seller.getStatusChangedAt()
        );
    }
}
