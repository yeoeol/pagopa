package com.commerce.pagopa.assistant.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.assistant.domain.AssistantConversation;
import com.commerce.pagopa.assistant.domain.AssistantConversationRepository;

public interface AssistantConversationJpaRepository
        extends
            JpaRepository<AssistantConversation, Long>,
            AssistantConversationRepository {
}
