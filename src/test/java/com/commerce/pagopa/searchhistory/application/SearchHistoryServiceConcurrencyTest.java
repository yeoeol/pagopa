package com.commerce.pagopa.searchhistory.application;

import com.commerce.pagopa.global.config.QueryDSLConfig;
import com.commerce.pagopa.searchhistory.domain.model.SearchHistory;
import com.commerce.pagopa.searchhistory.domain.repository.SearchHistoryRepository;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SearchHistoryService.class, TestcontainersConfig.class, QueryDSLConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class SearchHistoryServiceConcurrencyTest {

    private static final int PARTICIPANT_COUNT = 8;
    private static final long GATE_TIMEOUT_SECONDS = 5;
    private static final long COMPLETION_TIMEOUT_SECONDS = 15;
    private static final long TERMINATION_TIMEOUT_SECONDS = 5;

    @Autowired
    SearchHistoryService searchHistoryService;

    @Autowired
    SearchHistoryRepository searchHistoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void verify_mysql_database() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getDatabaseProductName())
                    .isEqualToIgnoringCase("MySQL");
        }
    }

    @Test
    @Tag("normal")
    void concurrent_different_keywords_are_all_saved() throws InterruptedException {
        String sessionId = unique_session_id("different-keywords");
        List<String> keywords = IntStream.range(0, PARTICIPANT_COUNT)
                .mapToObj(index -> "keyword-" + index)
                .toList();

        try {
            ConcurrencyResult result = execute_concurrently(
                    index -> searchHistoryService.saveHistory(null, sessionId, keywords.get(index))
            );

            assert_successful_execution(result);
            assertThat(searchHistoryRepository.findBySessionIdOrderByLastSearchedAtDesc(sessionId))
                    .hasSize(PARTICIPANT_COUNT)
                    .extracting(SearchHistory::getKeyword)
                    .containsExactlyInAnyOrderElementsOf(keywords);
        } finally {
            searchHistoryService.deleteAll(null, sessionId);
        }
    }

    @Test
    @Tag("exception")
    void concurrent_updates_of_existing_history_complete_without_errors() throws InterruptedException {
        String sessionId = unique_session_id("existing-history");
        String keyword = "keyword";
        Instant initialLastSearchedAt = Instant.parse("2000-01-01T00:00:00Z");
        SearchHistory existingHistory = searchHistoryRepository.save(
                SearchHistory.createForGuest(sessionId, keyword, initialLastSearchedAt)
        );
        Long initialId = existingHistory.getId();

        try {
            ConcurrencyResult result = execute_concurrently(
                    ignored -> searchHistoryService.saveHistory(null, sessionId, keyword)
            );

            assert_successful_execution(result);
            List<SearchHistory> histories =
                    searchHistoryRepository.findBySessionIdOrderByLastSearchedAtDesc(sessionId);
            assertThat(histories).hasSize(1);
            assertThat(histories.get(0).getId()).isEqualTo(initialId);
            assertThat(histories.get(0).getLastSearchedAt()).isAfter(initialLastSearchedAt);
        } finally {
            searchHistoryService.deleteAll(null, sessionId);
        }
    }

    @Test
    @Tag("boundary")
    void concurrent_whitespace_variants_are_deduplicated() throws InterruptedException {
        String sessionId = unique_session_id("whitespace-variants");
        List<String> keywordVariants = List.of(
                "keyword",
                " keyword ",
                "  keyword",
                "keyword  ",
                "keyword",
                " keyword ",
                "  keyword",
                "keyword  "
        );

        try {
            ConcurrencyResult result = execute_concurrently(
                    index -> searchHistoryService.saveHistory(null, sessionId, keywordVariants.get(index))
            );

            assert_successful_execution(result);
            assertThat(searchHistoryRepository.findBySessionIdOrderByLastSearchedAtDesc(sessionId))
                    .singleElement()
                    .extracting(SearchHistory::getKeyword)
                    .isEqualTo("keyword");
        } finally {
            searchHistoryService.deleteAll(null, sessionId);
        }
    }

    @Test
    @Tag("regression")
    void concurrent_same_user_keyword_results_in_one_history() throws InterruptedException {
        User user = userRepository.save(UserFixture.aUser(unique_suffix("same-user-keyword")));
        String keyword = "keyword";

        try {
            ConcurrencyResult result = execute_concurrently(
                    ignored -> searchHistoryService.saveHistory(user.getId(), null, keyword)
            );

            assert_successful_execution(result);
            assertThat(searchHistoryRepository.findByUserIdOrderByLastSearchedAtDesc(user.getId()))
                    .singleElement()
                    .extracting(SearchHistory::getKeyword)
                    .isEqualTo(keyword);
        } finally {
            searchHistoryService.deleteAll(user.getId(), null);
        }
    }

    @Test
    @Tag("regression")
    void concurrent_same_session_keyword_results_in_one_history() throws InterruptedException {
        String sessionId = unique_session_id("same-session-keyword");
        String keyword = "keyword";

        try {
            ConcurrencyResult result = execute_concurrently(
                    ignored -> searchHistoryService.saveHistory(null, sessionId, keyword)
            );

            assert_successful_execution(result);
            assertThat(searchHistoryRepository.findBySessionIdOrderByLastSearchedAtDesc(sessionId))
                    .singleElement()
                    .extracting(SearchHistory::getKeyword)
                    .isEqualTo(keyword);
        } finally {
            searchHistoryService.deleteAll(null, sessionId);
        }
    }

    private ConcurrencyResult execute_concurrently(IntConsumer operation) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(PARTICIPANT_COUNT);
        CyclicBarrier startGate = new CyclicBarrier(PARTICIPANT_COUNT);
        CountDownLatch completion = new CountDownLatch(PARTICIPANT_COUNT);
        AtomicInteger successCount = new AtomicInteger();
        ConcurrentLinkedQueue<Throwable> unexpectedFailures = new ConcurrentLinkedQueue<>();
        boolean completedWithinTimeout = false;
        long unfinishedTaskCount = PARTICIPANT_COUNT;
        boolean executorTerminated = false;

        try {
            for (int index = 0; index < PARTICIPANT_COUNT; index++) {
                int taskIndex = index;
                executor.submit(() -> {
                    try {
                        startGate.await(GATE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        operation.accept(taskIndex);
                        successCount.incrementAndGet();
                    } catch (Throwable throwable) {
                        if (throwable instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }
                        unexpectedFailures.add(throwable);
                    } finally {
                        completion.countDown();
                    }
                });
            }

            completedWithinTimeout = completion.await(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            unfinishedTaskCount = completion.getCount();
        } finally {
            executor.shutdownNow();
            executorTerminated = executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

        return new ConcurrencyResult(
                completedWithinTimeout,
                unfinishedTaskCount,
                executorTerminated,
                successCount.get(),
                0,
                List.copyOf(unexpectedFailures)
        );
    }

    private void assert_successful_execution(ConcurrencyResult result) {
        assertThat(result.completedWithinTimeout()).isTrue();
        assertThat(result.unfinishedTaskCount()).isZero();
        assertThat(result.executorTerminated()).isTrue();
        assertThat(result.outcomeCount()).isEqualTo(PARTICIPANT_COUNT);
        assertThat(result.expectedFailureCount()).isZero();
        assertThat(result.unexpectedFailures()).isEmpty();
        assertThat(result.successCount()).isEqualTo(PARTICIPANT_COUNT);
    }

    private String unique_session_id(String scenario) {
        return "search-history-" + scenario + "-" + UUID.randomUUID();
    }

    private String unique_suffix(String scenario) {
        return scenario + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private record ConcurrencyResult(
            boolean completedWithinTimeout,
            long unfinishedTaskCount,
            boolean executorTerminated,
            int successCount,
            int expectedFailureCount,
            List<Throwable> unexpectedFailures
    ) {

        int outcomeCount() {
            return successCount + expectedFailureCount + unexpectedFailures.size();
        }
    }
}
