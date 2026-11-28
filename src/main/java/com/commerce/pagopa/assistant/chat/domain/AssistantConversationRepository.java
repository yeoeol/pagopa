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

    // 상태 확인 후 변경(메시지 저장, 종료)하는 트랜잭션끼리 직렬화하기 위한 비관적 쓰기 락 조회
    Optional<AssistantConversation> findByIdForUpdate(Long conversationId);

    Optional<AssistantConversation> findByIdAndUserIdForUpdate(Long conversationId, Long userId);

    Page<AssistantConversation> findAllByUserIdOrderByUpdatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Page<AssistantConversation> findAllByUserIdAndStatusOrderByUpdatedAtDesc(
            Long userId,
            AssistantConversationStatus status,
            Pageable pageable
    );

    default AssistantConversation findByIdOrThrow(Long conversationId) {
        return findById(conversationId).orElseThrow(
                () -> new BusinessException(ErrorCode.ASSISTANT_CONVERSATION_NOT_FOUND)
        );
    }
}
