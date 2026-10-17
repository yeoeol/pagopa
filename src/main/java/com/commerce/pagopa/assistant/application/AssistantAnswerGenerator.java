package com.commerce.pagopa.assistant.application;

import reactor.core.publisher.Flux;

import com.commerce.pagopa.assistant.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.application.dto.response.GeneratedAnswerChunk;

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
