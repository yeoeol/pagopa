package com.commerce.pagopa.seller.application.admin.dto.response;

import com.commerce.pagopa.seller.domain.model.Seller;

import org.springframework.data.domain.Page;

import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public record AdminSellerPageResponseDto(
        List<AdminSellerListItemResponseDto> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        int startPage,
        int endPage
) {
    private static final int PAGE_WINDOW_SIZE = 10;

    public static AdminSellerPageResponseDto from(Page<Seller> sellers) {
        int totalPages = sellers.getTotalPages();
        int currentPage = sellers.getNumber();
        int startPage = max(0, currentPage - PAGE_WINDOW_SIZE / 2);
        int endPage = min(
                max(totalPages - 1, 0),
                startPage + PAGE_WINDOW_SIZE - 1
        );
        startPage = max(0, endPage - PAGE_WINDOW_SIZE + 1);

        return new AdminSellerPageResponseDto(
                sellers.getContent().stream()
                        .map(AdminSellerListItemResponseDto::from)
                        .toList(),
                currentPage,
                sellers.getSize(),
                sellers.getTotalElements(),
                totalPages,
                sellers.isFirst(),
                sellers.isLast(),
                startPage,
                endPage
        );
    }
}
