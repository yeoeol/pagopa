package com.commerce.pagopa.assistant.chat.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.chat.application.dto.response.AssistantHistoryMessageResponseDto;
import com.commerce.pagopa.assistant.chat.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversation;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationRepository;
import com.commerce.pagopa.assistant.chat.domain.AssistantMessage;
import com.commerce.pagopa.assistant.chat.domain.AssistantMessageRepository;
import com.commerce.pagopa.assistant.chat.domain.AssistantMessageRole;
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
        // 종료 트랜잭션과 직렬화: 락 없이 읽으면 커밋된 CLOSED를 못 보고 ACTIVE를 덮어쓴다
        AssistantConversation conversation = conversationRepository.findByIdAndUserIdForUpdate(
                conversationId,
                userId
        ).orElseThrow(() -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND));

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
                savedMessage.getRole().name(),
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
        AssistantConversation conversation = conversationRepository
                .findByIdForUpdate(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND));

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
        ).orElseThrow(() -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND));

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

    @Transactional
    public void delete(
            Long userId,
            Long conversationId,
            Long messageId
    ) {
        // 종료 트랜잭션과 직렬화: 락 없이 읽으면 종료 커밋 직전의 ACTIVE를 보고 닫힌 대화방 메시지를 삭제한다
        AssistantConversation conversation = conversationRepository.findByIdAndUserIdForUpdate(
                conversationId,
                userId
        ).orElseThrow(() -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND));

        if (!conversation.isActive()) {
            throw new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
        }

        AssistantMessage message = messageRepository.findByIdOrThrow(messageId);

        boolean inConversation = Objects.equals(
                message.getConversation().getId(),
                conversation.getId()
        );
        if (!inConversation || (message.getRole() != AssistantMessageRole.USER)) {
            throw new BusinessException(ErrorCode.ASSISTANT_MESSAGE_NOT_OWNER);
        }

        messageRepository.delete(message);
    }
}
