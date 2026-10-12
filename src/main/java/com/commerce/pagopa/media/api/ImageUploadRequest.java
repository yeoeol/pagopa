package com.commerce.pagopa.media.api;

import com.commerce.pagopa.media.domain.ImageCategory;

public record ImageUploadRequest(
        ImageCategory imageCategory,
        String originalFilename,
        String contentType,
        long size,
        ContentSource content
) {
}
