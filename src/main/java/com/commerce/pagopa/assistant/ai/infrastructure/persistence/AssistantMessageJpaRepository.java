package com.commerce.pagopa.assistant.ai.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.assistant.ai.domain.AssistantMessage;
import com.commerce.pagopa.assistant.ai.domain.AssistantMessageRepository;

public interface AssistantMessageJpaRepository
        extends
            JpaRepository<AssistantMessage, Long>,
            AssistantMessageRepository {
}
