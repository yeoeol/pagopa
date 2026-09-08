package com.commerce.pagopa.global.seeder;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(
        boolean enabled,
        int batchSize,
        Counts counts
) {
    public record Counts(
            int users,
            int products,
            int carts,
            int orders,
            int orderItems,
            int payments,
            int reviews,
            int reviewImages,
            int searchHistories
    ) {}
}
