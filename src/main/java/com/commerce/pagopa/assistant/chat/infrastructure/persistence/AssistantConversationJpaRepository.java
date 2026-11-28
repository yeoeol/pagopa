package com.commerce.pagopa.assistant.chat.infrastructure.persistence;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.commerce.pagopa.assistant.chat.domain.AssistantConversation;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationRepository;

public interface AssistantConversationJpaRepository
        extends JpaRepository<AssistantConversation, Long>, AssistantConversationRepository {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM AssistantConversation c WHERE c.id = :conversationId")
    Optional<AssistantConversation> findByIdForUpdate(@Param("conversationId") Long conversationId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT c
            FROM AssistantConversation c
            WHERE c.id = :conversationId AND c.userId = :userId
            """)
    Optional<AssistantConversation> findByIdAndUserIdForUpdate(
            @Param("conversationId") Long conversationId,
            @Param("userId") Long userId
    );
}
