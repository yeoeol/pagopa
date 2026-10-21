package com.commerce.pagopa.assistant.knowledge.infrastructure.config;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AssistantRagProperties.class)
@ConditionalOnProperty(
        name = "app.assistant.rag.enabled",
        havingValue = "true"
)
public class AssistantRagConfig {

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(
            VectorStore vectorStore,
            AssistantRagProperties properties
    ) {
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(properties.topK())
                .similarityThreshold(properties.similarityThreshold())
                .build();

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();
    }
}
