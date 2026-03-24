package com.commerce.pagopa.domain.image.dto.response;

public record ImageResponseDto(
        String imageUrl
) {
    public static ImageResponseDto of(String imageUrl) {
        return new ImageResponseDto(imageUrl);
    }
}
