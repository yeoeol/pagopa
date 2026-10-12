package com.commerce.pagopa.media.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.event.UserProfileUpdated;
import com.commerce.pagopa.review.event.ReviewDeleted;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final ImageService imageService;

    @ApplicationModuleListener
    void on(UserProfileUpdated event) {
        imageService.delete(event.oldProfileImageUrl());
    }

    @ApplicationModuleListener
    void on(ReviewDeleted event) {
        RuntimeException failure = null;
        for (String imageUrl : event.imageUrls()) {
            try {
                imageService.delete(imageUrl);
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(failure);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
