package com.commerce.pagopa.assistant.chat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.assistant.chat.domain.AssistantConversation;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationRepository;

public interface AssistantConversationJpaRepository
        extends
            JpaRepository<AssistantConversation, Long>,
            AssistantConversationRepository {
}
