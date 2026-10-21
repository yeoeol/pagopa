package com.commerce.pagopa.assistant.knowledge.application;

import java.util.List;

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
        List<AssistantKnowledgeSource> sources = documentReader.readAll();
        sources.forEach(this::replace);

        assistantKnowledgeService.deleteExcept(
                sources.stream().map(AssistantKnowledgeSource::sourceId).toList()
        );
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
