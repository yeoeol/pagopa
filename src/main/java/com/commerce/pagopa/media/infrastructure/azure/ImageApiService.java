package com.commerce.pagopa.media.infrastructure.azure;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.media.api.ImageApi;
import com.commerce.pagopa.media.application.ImageService;

@Service
@RequiredArgsConstructor
public class ImageApiService implements ImageApi {

    private final ImageService imageService;

    @Override
    public void delete(String profileImageUrl) {
        imageService.delete(profileImageUrl);
    }
}
