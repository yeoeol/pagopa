package com.commerce.pagopa.assistant.knowledge.application;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AssistantKnowledgeServiceTest {

    private final VectorStore vectorStore = mock(VectorStore.class);

    private final AssistantKnowledgeService assistantKnowledgeService =
            new AssistantKnowledgeService(vectorStore);

    @Test
    void save_document_chunk_in_vectorStore() {
        String content = """
                반품 정책입니다.
                상품 수령 후 7일 이내에 반품을 신청할 수 있습니다.
                단순 변심 반품의 배송비는 구매자가 부담합니다.
                """.repeat(20);

        assistantKnowledgeService.replace(
                "return-policy",
                "반품 및 환불 정책",
                "POLICY",
                content
        );

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> saved = captor.getValue();
        assertThat(saved).isNotEmpty();
        assertThat(saved)
                .allSatisfy(document -> {
                    assertThat(document.getMetadata().get("sourceId")).isEqualTo("return-policy");
                    assertThat(document.getMetadata().get("title")).isEqualTo("반품 및 환불 정책");
                });
    }
}
