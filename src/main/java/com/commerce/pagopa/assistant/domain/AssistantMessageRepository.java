package com.commerce.pagopa.assistant.domain;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

public interface AssistantMessageRepository {

    AssistantMessage save(AssistantMessage message);

    Optional<AssistantMessage> findById(Long messageId);

    Slice<AssistantMessage> findAllByConversationIdOrderByIdDesc(
            Long conversationId,
            Pageable pageable
    );

    default AssistantMessage findByIdOrThrow(Long messageId) {
        return findById(messageId).orElseThrow(
                () -> new BusinessException(ErrorCode.ASSISTANT_MESSAGE_NOT_FOUND)
        );
    }
}
