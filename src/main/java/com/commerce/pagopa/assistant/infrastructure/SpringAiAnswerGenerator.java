package com.commerce.pagopa.assistant.infrastructure;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.application.AssistantAnswerGenerator;
import com.commerce.pagopa.assistant.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.application.exception.AssistantModelException;

@Component
@RequiredArgsConstructor
public class SpringAiAnswerGenerator implements AssistantAnswerGenerator {

    private final ChatClient chatClient;

    @Override
    public GeneratedAnswer generate(
            Long conversationId,
            String question
    ) {
        try {
            ChatResponse response = chatClient.prompt()
                    .user(question)
                    .advisors(
                            advisor -> advisor.param(
                                    ChatMemory.CONVERSATION_ID,
                                    conversationId.toString()
                            )
                    )
                    .call()
                    .chatResponse();

            return convert(response);
        } catch (TransientAiException | NonTransientAiException exception) {
            throw new AssistantModelException(
                    "Ollama 응답 생성에 실패했습니다.",
                    exception
            );
        }
    }

    private GeneratedAnswer convert(ChatResponse response) {
        if (response == null) {
            throw new AssistantModelException(
                    "Ollama가 응답을 반환하지 않았습니다."
            );
        }

        Generation generation = response.getResult();
        if (generation == null) {
            throw new AssistantModelException(
                    "Ollama 응답에 생성 결과가 없습니다."
            );
        }

        String content = generation.getOutput().getText();

        if (!StringUtils.hasText(content)) {
            throw new AssistantModelException(
                    "Ollama가 빈 답변을 반환했습니다."
            );
        }

        Usage usage = response.getMetadata().getUsage();
        return new GeneratedAnswer(
                content,
                usage.getPromptTokens(),
                usage.getCompletionTokens()
        );
    }
}
