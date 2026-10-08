package com.commerce.pagopa.merchant.application.admin.dto.response;

import java.time.LocalDateTime;

import com.commerce.pagopa.global.response.StatusResponseDto;
import com.commerce.pagopa.identity.api.UserSummary;
import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerStatus;
import com.commerce.pagopa.merchant.domain.VerificationStatus;

public record AdminSellerListItemResponseDto(Long sellerId, Long userId, String name, String email,
        StatusResponseDto<SellerStatus> status, StatusResponseDto<VerificationStatus> verificationStatus,
        LocalDateTime requestedAt) {
    public static AdminSellerListItemResponseDto from(Seller seller, UserSummary summary) {
        return new AdminSellerListItemResponseDto(seller.getId(), summary.userId(), summary.name(), summary.email(),
                StatusResponseDto.from(seller.getStatus()), StatusResponseDto.from(seller.getVerificationStatus()),
                seller.getStatusChangedAt());
    }
}
