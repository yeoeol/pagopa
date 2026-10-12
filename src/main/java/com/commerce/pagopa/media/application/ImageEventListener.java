package com.commerce.pagopa.media.application;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.identity.event.UserProfileUpdated;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final ImageService imageService;

    @ApplicationModuleListener
    void on(UserProfileUpdated event) {
        imageService.delete(event.oldProfileImageUrl());
    }
}
