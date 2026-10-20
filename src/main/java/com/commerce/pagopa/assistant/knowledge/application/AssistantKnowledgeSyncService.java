package com.commerce.pagopa.assistant.knowledge.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Service
@RequiredArgsConstructor
public class AssistantKnowledgeSyncService {

    private final AssistantKnowledgeService assistantKnowledgeService;
    private final AssistantKnowledgeDocumentReader documentReader;

    public void syncAll() {
        documentReader.readAll()
                .forEach(this::replace);
    }

    public void sync(String sourceId) {
        AssistantKnowledgeSource source = documentReader.readBySourceId(sourceId)
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.KNOWLEDGE_DOCUMENT_NOT_FOUND,
                                "지식 문서를 찾을 수 없습니다: " + sourceId
                        )
                );

        replace(source);
    }

    private void replace(AssistantKnowledgeSource source) {
        assistantKnowledgeService.replace(
                source.sourceId(),
                source.title(),
                source.type(),
                source.content()
        );
    }
}
