package com.commerce.pagopa.image.application.dto.response;

public record ImageResponseDto(
        String imageUrl
) {
    public static ImageResponseDto of(String imageUrl) {
        return new ImageResponseDto(imageUrl);
    }
}
