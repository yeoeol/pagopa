package com.commerce.pagopa.assistant.knowledge.application;

import java.time.Duration;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.commerce.pagopa.support.testcontainers.MySqlIntegrationTest;
import com.commerce.pagopa.support.testcontainers.RagTestcontainersConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        properties = {
                "spring.ai.vectorstore.type=elasticsearch"
        }
)
@Import({RagTestcontainersConfig.class})
@MySqlIntegrationTest
@EnabledIfEnvironmentVariable(
        named = "RUN_RAG_E2E",
        matches = "true"
)
class AssistantKnowledgeVectorStoreIntegrationTest {

    private static final String RETURN_POLICY_ID = "it-return-policy";
    private static final String DELIVERY_POLICY_ID = "it-delivery-policy";

    @Autowired
    AssistantKnowledgeService assistantKnowledgeService;

    @Autowired
    VectorStore vectorStore;

    @AfterEach
    void cleanUp() {
        deleteTestDocuments();
    }

    @Test
    void can_search_stored_policy_based_meaning() {
        assistantKnowledgeService.replace(
                RETURN_POLICY_ID,
                "반품 및 환불 정책",
                "POLICY",
                """
                상품을 수령한 날부터 7일 이내에 반품을 신청할 수 있습니다.
                단순 변심으로 반품하는 경우 왕복 배송비는 구매자가 부담합니다.
                상품에 하자가 있으면 판매자가 배송비를 부담합니다.
                """
        );

        assistantKnowledgeService.replace(
                DELIVERY_POLICY_ID,
                "배송 정책",
                "POLICY",
                """
                결제가 완료된 상품은 영업일 기준 2일 이내에 출고됩니다.
                제주도와 도서 산간 지역은 배송이 지연될 수 있습니다.
                """
        );

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<Document> result = vectorStore.similaritySearch(
                            SearchRequest.builder()
                                    .query("물건을 받고 며칠 안에 반품할 수 있나요?")
                                    .topK(1)
                                    .similarityThreshold(0.0)
                                    .build()
                    );
                    assertThat(result).hasSize(1);

                    Document mostRelevantDocument = result.getFirst();

                    assertThat(mostRelevantDocument.getMetadata())
                            .containsEntry("sourceId", RETURN_POLICY_ID)
                            .containsEntry("title", "반품 및 환불 정책")
                            .containsEntry("type", "POLICY");

                    assertThat(mostRelevantDocument.getText())
                            .contains("7일 이내");

                    assertThat(mostRelevantDocument.getScore())
                            .isNotNull()
                            .isGreaterThan(0.0);
                });


    }

    private void deleteTestDocuments() {
        vectorStore.delete("sourceId == '" + RETURN_POLICY_ID + "'");
        vectorStore.delete("sourceId == '" + DELIVERY_POLICY_ID + "'");
    }
}
