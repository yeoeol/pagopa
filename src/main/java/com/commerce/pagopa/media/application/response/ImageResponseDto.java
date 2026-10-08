package com.commerce.pagopa.media.application.response;

public record ImageResponseDto(String imageUrl) {
    public static ImageResponseDto of(String imageUrl) {
        return new ImageResponseDto(imageUrl);
    }
}
