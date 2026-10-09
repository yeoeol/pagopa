package com.commerce.pagopa.media.infrastructure.azure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.provider.azure")
public record AzureStorageProperties(
        String containerName,
        String baseUrl
) {
}
