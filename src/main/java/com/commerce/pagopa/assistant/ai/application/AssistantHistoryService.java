package com.commerce.pagopa.assistant.ai.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.ai.application.dto.response.AssistantHistoryMessageResponseDto;
import com.commerce.pagopa.assistant.ai.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.ai.domain.AssistantConversation;
import com.commerce.pagopa.assistant.ai.domain.AssistantConversationRepository;
import com.commerce.pagopa.assistant.ai.domain.AssistantMessage;
import com.commerce.pagopa.assistant.ai.domain.AssistantMessageRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Service
@RequiredArgsConstructor
public class AssistantHistoryService {

    private final AssistantConversationRepository conversationRepository;
    private final AssistantMessageRepository messageRepository;

    @Transactional
    public AssistantHistoryMessageResponseDto saveUserMessage(
            Long userId,
            Long conversationId,
            String message
    ) {
        AssistantConversation conversation = conversationRepository.findByIdAndUserId(
                conversationId,
                userId
        )
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND)
                );

        if (!conversation.isActive()) {
            throw new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
        }

        AssistantMessage savedMessage = messageRepository.save(
                AssistantMessage.user(
                        conversation,
                        message
                )
        );

        conversation.renewUpdatedAt();
        return new AssistantHistoryMessageResponseDto(
                conversation.getId(),
                savedMessage.getId(),
                savedMessage.getRole()
                        .name(),
                savedMessage.getContent(),
                savedMessage.getInputTokens(),
                savedMessage.getOutputTokens(),
                savedMessage.getCreatedAt()
        );
    }

    @Transactional
    public AssistantHistoryMessageResponseDto saveAssistantMessage(
            Long conversationId,
            GeneratedAnswer answer
    ) {
        AssistantConversation conversation = conversationRepository.findByIdOrThrow(conversationId);

        if (!conversation.isActive()) {
            throw new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
        }

        AssistantMessage savedMessage = messageRepository.save(
                AssistantMessage.assistant(
                        conversation,
                        answer.content(),
                        answer.inputTokens(),
                        answer.outputTokens()
                )
        );

        conversation.renewUpdatedAt();
        return new AssistantHistoryMessageResponseDto(
                conversation.getId(),
                savedMessage.getId(),
                savedMessage.getRole()
                        .name(),
                savedMessage.getContent(),
                savedMessage.getInputTokens(),
                savedMessage.getOutputTokens(),
                savedMessage.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<AssistantHistoryMessageResponseDto> getAll(
            Long userId,
            Long conversationId,
            Pageable pageable
    ) {
        AssistantConversation conversation = conversationRepository.findByIdAndUserId(
                conversationId,
                userId
        )
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND)
                );

        Slice<AssistantMessage> messages = messageRepository
                .findAllByConversationIdOrderByIdDesc(
                        conversation.getId(),
                        pageable
                );

        List<AssistantHistoryMessageResponseDto> result = new ArrayList<>();
        messages.forEach(
                message -> result.add(
                        new AssistantHistoryMessageResponseDto(
                                conversation.getId(),
                                message.getId(),
                                message.getRole()
                                        .name(),
                                message.getContent(),
                                message.getInputTokens(),
                                message.getOutputTokens(),
                                message.getCreatedAt()
                        )
                )
        );
        return result;
    }

    @Transactional
    public void markGenerationFailed(Long messageId) {
        AssistantMessage message = messageRepository.findByIdOrThrow(messageId);
        message.fail();
    }
}
