package com.commerce.pagopa.assistant.knowledge.application;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssistantKnowledgeService {

    private final VectorStore vectorStore;

    public void replace(
            String sourceId,
            String title,
            String type,
            String content
    ) {
        delete(sourceId);

        Document source = Document.builder()
                .id(sourceId)
                .text(content)
                .metadata("sourceId", sourceId)
                .metadata("title", title)
                .metadata("type", type)
                .build();

        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(20)
                .withKeepSeparator(true)
                .build();

        List<Document> splitDocuments = splitter.apply(List.of(source));

        List<Document> indexedDocuments = IntStream
                .range(0, splitDocuments.size())
                .mapToObj(index -> splitDocuments.get(index)
                        .mutate()
                        .id(sourceId + "::chunk::" + index)
                        .metadata("chunkNo", index)
                        .build()
                )
                .toList();

        vectorStore.add(indexedDocuments);
    }

    private void delete(String sourceId) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        vectorStore.delete(
                builder.eq("sourceId", sourceId).build()
        );
    }
}
