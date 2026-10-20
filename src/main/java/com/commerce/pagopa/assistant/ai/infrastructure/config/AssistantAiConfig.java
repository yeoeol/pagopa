package com.commerce.pagopa.assistant.ai.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantAiConfig {

    private final static String SYSTEM_PROMPT = """
                        당신은 쇼핑몰 고객 지원 도우미입니다.
                        검색된 문서만 사실의 근거로 사용합니다.
                        문서에 근거가 없으면 추측하지 말고
                        확인할 수 없다고 답변합니다.
                        서로 충돌하는 문서가 있으면 최신 유효 문서를 우선합니다.
                        """;

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            ObjectProvider<QuestionAnswerAdvisor> advisorProvider
    ) {
        ChatClient.Builder configured = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                );

        advisorProvider.ifAvailable(
                configured::defaultAdvisors
        );

        return configured.build();
    }

    @Bean
    ChatMemory chatMemory(
            ChatMemoryRepository chatMemoryRepository
    ) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(12)
                .build();
    }
}
