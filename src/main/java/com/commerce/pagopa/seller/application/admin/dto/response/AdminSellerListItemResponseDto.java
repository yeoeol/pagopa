package com.commerce.pagopa.seller.application.admin.dto.response;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.model.enums.SellerStatus;
import com.commerce.pagopa.seller.domain.model.enums.VerificationStatus;

import java.time.Instant;

public record AdminSellerListItemResponseDto(
        Long sellerId,
        Long userId,
        String name,
        String email,
        StatusResponseDto<SellerStatus> status,
        StatusResponseDto<VerificationStatus> verificationStatus,
        Instant requestedAt
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
