package com.commerce.pagopa.assistant.knowledge.application;

import java.util.List;
import java.util.Optional;

public interface AssistantKnowledgeDocumentReader {

    List<AssistantKnowledgeSource> readAll();

    Optional<AssistantKnowledgeSource> readBySourceId(String sourceId);
}
