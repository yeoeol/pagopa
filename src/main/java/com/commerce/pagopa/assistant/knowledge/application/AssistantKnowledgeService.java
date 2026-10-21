package com.commerce.pagopa.assistant.knowledge.application;

import java.util.ArrayList;
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
        deleteStableChunks(sourceId, indexedDocuments.size());
    }

    public void deleteExcept(List<String> sourceIds) {
        if (sourceIds.isEmpty()) {
            return;
        }
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        vectorStore.delete(
                builder.nin("sourceId", new ArrayList<>(sourceIds)).build()
        );
    }

    private void deleteStableChunks(String sourceId, int chunkCount) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        vectorStore.delete(
                builder.and(
                        builder.eq("sourceId", sourceId),
                        builder.gte("chunkNo", chunkCount)
                ).build()
        );
    }
}
