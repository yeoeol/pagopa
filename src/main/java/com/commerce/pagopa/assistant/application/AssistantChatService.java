package com.commerce.pagopa.assistant.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.application.dto.request.AssistantMessageRequestDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantHistoryMessageResponseDto;
import com.commerce.pagopa.assistant.application.dto.response.AssistantMessageResponseDto;
import com.commerce.pagopa.assistant.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.application.exception.AssistantModelException;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Service
@RequiredArgsConstructor
public class AssistantChatService {

    private final AssistantAnswerGenerator answerGenerator;
    private final AssistantHistoryService historyService;

    public AssistantMessageResponseDto chat(
            Long userId,
            Long conversationId,
            AssistantMessageRequestDto requestDto
    ) {
        AssistantHistoryMessageResponseDto userMessage = historyService.saveUserMessage(
                userId,
                conversationId,
                requestDto.message()
        );

        GeneratedAnswer answer;
        try {
            answer = answerGenerator.generate(
                    conversationId,
                    requestDto.message()
            );
        } catch (AssistantModelException exception) {
            throw new BusinessException(
                    ErrorCode.ASSISTANT_MODEL_UNAVAILABLE,
                    exception.getMessage()
            );
        }

        AssistantHistoryMessageResponseDto assistantMessage = historyService.saveAssistantMessage(
                conversationId,
                answer
        );

        return new AssistantMessageResponseDto(
                conversationId,
                userMessage.messageId(),
                assistantMessage.messageId(),
                answer.content(),
                answer.inputTokens(),
                answer.outputTokens()
        );
    }
}
