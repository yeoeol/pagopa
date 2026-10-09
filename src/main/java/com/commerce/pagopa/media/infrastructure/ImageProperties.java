package com.commerce.pagopa.media.infrastructure;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.image")
public record ImageProperties(
        long maxSize,
        List<String> allowedTypes
) {
}
