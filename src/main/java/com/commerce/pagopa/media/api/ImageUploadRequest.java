package com.commerce.pagopa.media.api;

public record ImageUploadRequest(
        ImageCategory imageCategory,
        String originalFilename,
        String contentType,
        long size,
        ContentSource content
) {
}
