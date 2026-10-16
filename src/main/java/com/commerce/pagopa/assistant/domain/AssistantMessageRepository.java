package com.commerce.pagopa.assistant.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface AssistantMessageRepository {

    AssistantMessage save(AssistantMessage message);

    Slice<AssistantMessage> findAllByConversationIdOrderByIdDesc(
            Long conversationId,
            Pageable pageable
    );
}
