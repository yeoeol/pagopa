package com.commerce.pagopa.assistant.chat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.commerce.pagopa.assistant.chat.domain.AssistantMessage;
import com.commerce.pagopa.assistant.chat.domain.AssistantMessageRepository;

public interface AssistantMessageJpaRepository
        extends
            JpaRepository<AssistantMessage, Long>,
            AssistantMessageRepository {
}
