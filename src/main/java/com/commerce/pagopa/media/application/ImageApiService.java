package com.commerce.pagopa.media.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.media.api.ImageApi;
import com.commerce.pagopa.media.api.ImageUploadRequest;
import com.commerce.pagopa.media.api.ImageUploadResult;
import com.commerce.pagopa.media.application.response.ImageResponseDto;

@Service
@RequiredArgsConstructor
public class ImageApiService implements ImageApi {

    private final ImageService imageService;

    @Override
    public ImageUploadResult upload(ImageUploadRequest command) {
        ImageResponseDto response = imageService.upload(command);
        return new ImageUploadResult(
                response.imageUrl()
        );
    }
}
