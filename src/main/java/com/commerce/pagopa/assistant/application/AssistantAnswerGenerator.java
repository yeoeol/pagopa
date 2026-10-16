package com.commerce.pagopa.assistant.application;

import com.commerce.pagopa.assistant.application.dto.response.GeneratedAnswer;

public interface AssistantAnswerGenerator {

    GeneratedAnswer generate(
            Long conversationId,
            String question
    );
}
