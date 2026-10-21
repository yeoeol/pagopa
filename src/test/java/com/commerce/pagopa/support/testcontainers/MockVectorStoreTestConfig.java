package com.commerce.pagopa.support.testcontainers;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
@ConditionalOnProperty(
        name = "spring.ai.vectorstore.type",
        havingValue = "none"
)
public class MockVectorStoreTestConfig {

    @Bean
    VectorStore vectorStore() {
        return mock(VectorStore.class);
    }
}
