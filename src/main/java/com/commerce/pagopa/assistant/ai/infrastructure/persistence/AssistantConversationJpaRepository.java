package com.commerce.pagopa.assistant.ai.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.assistant.ai.domain.AssistantConversation;
import com.commerce.pagopa.assistant.ai.domain.AssistantConversationRepository;

public interface AssistantConversationJpaRepository
        extends
            JpaRepository<AssistantConversation, Long>,
            AssistantConversationRepository {
}
