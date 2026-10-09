package com.commerce.pagopa.media.infrastructure.supabase;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.provider.supabase")
public record SupabaseStorageProperties(
        URI endpoint,
        String region,
        String accessKey,
        String secretKey,
        String bucket,
        URI projectUrl
) {
}
