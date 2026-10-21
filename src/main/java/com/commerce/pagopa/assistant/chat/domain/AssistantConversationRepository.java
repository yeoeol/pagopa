package com.commerce.pagopa.assistant.chat.domain;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

public interface AssistantConversationRepository {

    AssistantConversation save(AssistantConversation conversation);

    Optional<AssistantConversation> findById(Long conversationId);

    Optional<AssistantConversation> findByIdAndUserId(Long conversationId, Long userId);

    Page<AssistantConversation> findAllByUserIdOrderByUpdatedAtDesc(
            Long userId,
            Pageable pageable
    );

    default AssistantConversation findByIdOrThrow(Long conversationId) {
        return findById(conversationId).orElseThrow(
                () -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND)
        );
    }
}
