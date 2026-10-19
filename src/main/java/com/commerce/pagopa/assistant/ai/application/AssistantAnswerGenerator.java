package com.commerce.pagopa.assistant.ai.application;

import reactor.core.publisher.Flux;

import com.commerce.pagopa.assistant.ai.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.ai.application.dto.response.GeneratedAnswerChunk;

public interface AssistantAnswerGenerator {

    GeneratedAnswer generate(
            Long conversationId,
            String question
    );

    Flux<GeneratedAnswerChunk> generateStream(
            Long conversationId,
            String question
    );
}
