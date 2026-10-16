package com.commerce.pagopa.assistant.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.assistant.domain.AssistantMessage;
import com.commerce.pagopa.assistant.domain.AssistantMessageRepository;

public interface AssistantMessageJpaRepository
        extends
            JpaRepository<AssistantMessage, Long>,
            AssistantMessageRepository {
}
