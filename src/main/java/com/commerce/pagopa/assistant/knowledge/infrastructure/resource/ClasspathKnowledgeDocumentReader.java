package com.commerce.pagopa.assistant.knowledge.infrastructure.resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.assistant.knowledge.application.AssistantKnowledgeDocumentReader;
import com.commerce.pagopa.assistant.knowledge.application.AssistantKnowledgeSource;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.assistant.knowledge",
        name = "source",
        havingValue = "classpath",
        matchIfMissing = true
)
public class ClasspathKnowledgeDocumentReader implements AssistantKnowledgeDocumentReader {

    private final ResourcePatternResolver resourcePatternResolver;

    private static final List<KnowledgeLocation> LOCATIONS = List.of(
            new KnowledgeLocation(
                    "POLICY",
                    "classpath*:assistant/knowledge/policy/*.md"
            ),
            new KnowledgeLocation(
                    "FAQ",
                    "classpath*:assistant/knowledge/faq/*.md"
            )
    );

    @Override
    public List<AssistantKnowledgeSource> readAll() {
        List<AssistantKnowledgeSource> sources = new ArrayList<>();

        for (KnowledgeLocation location : LOCATIONS) {
            sources.addAll(readResources(location));
        }

        validateDuplicateSourceIds(sources);

        return sources.stream()
                .sorted(Comparator.comparing(AssistantKnowledgeSource::sourceId))
                .toList();
    }

    @Override
    public Optional<AssistantKnowledgeSource> readBySourceId(String sourceId) {
        return readAll().stream()
                .filter(source -> source.sourceId().equals(sourceId))
                .findFirst();
    }

    private List<AssistantKnowledgeSource> readResources(KnowledgeLocation location) {
        try {
            return Arrays.stream(
                    resourcePatternResolver.getResources(location.pattern)
            )
            .map(resource -> toKnowledgeSource(resource, location.type))
            .toList();
        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.KNOWLEDGE_DOCUMENT_NOT_READABLE,
                    "지식 문서 목록을 읽을 수 없습니다: " + location.pattern()
            );
        }
    }

    private AssistantKnowledgeSource toKnowledgeSource(
            Resource resource,
            String type
    ) {
        try {
            String content = resource.getContentAsString(
                    StandardCharsets.UTF_8
            );

            return new AssistantKnowledgeSource(
                    extractSourceId(resource),
                    extractTitle(content, resource),
                    type,
                    content
            );
        } catch (IOException exception) {
            throw new BusinessException(
                    ErrorCode.KNOWLEDGE_DOCUMENT_NOT_READABLE,
                    "지식 문서를 읽을 수 없습니다: " + resource.getDescription()
            );
        }
    }

    private String extractSourceId(Resource resource) {
        String filename = resource.getFilename();

        if (filename == null || !filename.endsWith(".md")) {
            throw new BusinessException(
                    ErrorCode.KNOWLEDGE_DOCUMENT_NOT_READABLE,
                    "올바르지 않은 지식 문서 파일입니다: " + resource.getDescription()
            );
        }

        return filename.substring(
                0,
                filename.length() - ".md".length()
        );
    }

    private String extractTitle(String content, Resource resource) {
        return content.lines()
                .map(String::strip)
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).strip())
                .filter(title -> !title.isBlank())
                .findFirst()
                .orElseThrow(
                        () -> new BusinessException(
                                ErrorCode.KNOWLEDGE_DOCUMENT_NOT_READABLE,
                                "지식 문서의 제목을 찾을 수 없습니다:: " + resource.getDescription()
                        )
                );
    }

    private void validateDuplicateSourceIds(List<AssistantKnowledgeSource> sources) {
        Set<String> sourcesIds = new HashSet<>();

        for (AssistantKnowledgeSource source : sources) {
            if (!sourcesIds.add(source.sourceId())) {
                throw new BusinessException(
                        ErrorCode.DUPLICATE_KNOWLEDGE_DOCUMENT,
                        "중복된 지식 문서 sourceId입니다: " + source.sourceId()
                );
            }
        }
    }

    private record KnowledgeLocation(
            String type,
            String pattern
    ) {}
}
