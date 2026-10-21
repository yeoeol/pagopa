package com.commerce.pagopa.assistant.knowledge.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.assistant.rag")
public record AssistantRagProperties(
        boolean enabled,
        int topK,
        double similarityThreshold
) {
}
