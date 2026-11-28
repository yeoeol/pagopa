package com.commerce.pagopa.assistant.chat.application;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.commerce.pagopa.assistant.chat.application.dto.response.GeneratedAnswer;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversation;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationRepository;
import com.commerce.pagopa.assistant.chat.domain.AssistantConversationStatus;
import com.commerce.pagopa.assistant.chat.domain.AssistantMessageRepository;
import com.commerce.pagopa.global.config.JpaConfig;
import com.commerce.pagopa.global.config.QueryDSLConfig;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.identity.domain.User;
import com.commerce.pagopa.identity.domain.UserRepository;
import com.commerce.pagopa.support.fixture.UserFixture;
import com.commerce.pagopa.support.testcontainers.DatabaseCleaner;
import com.commerce.pagopa.support.testcontainers.MySqlIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@MySqlIntegrationTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        AssistantHistoryService.class,
        AssistantConversationService.class,
        QueryDSLConfig.class,
        JpaConfig.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AssistantConversationConcurrencyTest {

    private static final long BLOCK_CHECK_MILLIS = 1_000;
    private static final long TIMEOUT_SECONDS = 15;

    @Autowired
    AssistantHistoryService historyService;

    @Autowired
    AssistantConversationService conversationService;

    @Autowired
    AssistantConversationRepository conversationRepository;

    @Autowired
    AssistantMessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    DatabaseCleaner databaseCleaner;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private TransactionTemplate transactionTemplate;
    private Long userId;
    private Long conversationId;

    @BeforeEach
    void setUp() throws SQLException {
        databaseCleaner.clean();
        transactionTemplate = new TransactionTemplate(transactionManager);

        User user = userRepository.save(
                UserFixture.aUser(
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 12)
                )
        );
        userId = user.getId();
        conversationId = conversationRepository.save(AssistantConversation.start(userId, "배송 문의"))
                .getId();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("종료 트랜잭션이 커밋 전이면 사용자 메시지 저장은 대기했다가 CLOSED 예외로 실패하고 대화는 CLOSED로 남는다")
    void saveUserMessage_waitsForClose_thenFailsWithClosed() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch releaseClose = new CountDownLatch(1);

        Future<?> closeTask = executor.submit(
                () -> transactionTemplate.executeWithoutResult(status -> {
                    conversationService.close(userId, conversationId);
                    locked.countDown();
                    await(releaseClose);
                })
        );
        assertThat(locked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        Future<?> messageTask = executor.submit(
                () -> historyService.saveUserMessage(userId, conversationId, "안녕하세요")
        );

        assertBlocked(messageTask);
        releaseClose.countDown();
        closeTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThatThrownBy(() -> messageTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .hasCauseInstanceOf(BusinessException.class)
                .cause()
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
        assertClosedWithMessageCount(0);
    }

    @Test
    @DisplayName("종료 트랜잭션이 커밋 전이면 어시스턴트 메시지 저장도 대기했다가 CLOSED 예외로 실패한다")
    void saveAssistantMessage_waitsForClose_thenFailsWithClosed() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch releaseClose = new CountDownLatch(1);

        Future<?> closeTask = executor.submit(
                () -> transactionTemplate.executeWithoutResult(status -> {
                    conversationService.close(userId, conversationId);
                    locked.countDown();
                    await(releaseClose);
                })
        );
        assertThat(locked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        Future<?> messageTask = executor.submit(
                () -> historyService.saveAssistantMessage(
                        conversationId,
                        new GeneratedAnswer("답변", 1L, 1L)
                )
        );

        assertBlocked(messageTask);
        releaseClose.countDown();
        closeTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThatThrownBy(() -> messageTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .hasCauseInstanceOf(BusinessException.class)
                .cause()
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
        assertClosedWithMessageCount(0);
    }

    @Test
    @DisplayName("메시지 트랜잭션이 커밋 전이면 종료는 대기했다가 성공하고, 메시지 커밋이 CLOSED를 ACTIVE로 덮어쓰지 않는다")
    void close_waitsForMessage_thenRemainsClosed() throws Exception {
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch releaseMessage = new CountDownLatch(1);

        Future<?> messageTask = executor.submit(
                () -> transactionTemplate.executeWithoutResult(status -> {
                    historyService.saveUserMessage(userId, conversationId, "안녕하세요");
                    locked.countDown();
                    await(releaseMessage);
                })
        );
        assertThat(locked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();

        Future<?> closeTask = executor.submit(
                () -> conversationService.close(userId, conversationId)
        );

        assertBlocked(closeTask);
        releaseMessage.countDown();
        messageTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        closeTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertClosedWithMessageCount(1);
    }

    @Test
    @DisplayName("이미 종료된 대화에 메시지를 저장하면 CLOSED 예외를 던지고 메시지는 저장되지 않는다")
    void saveUserMessage_closedConversation_throwsClosed() {
        conversationService.close(userId, conversationId);

        assertThatThrownBy(() -> historyService.saveUserMessage(userId, conversationId, "안녕하세요"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ASSISTANT_CONVERSATION_CLOSED);
        assertClosedWithMessageCount(0);
    }

    private void assertBlocked(Future<?> task) {
        assertThatThrownBy(() -> task.get(BLOCK_CHECK_MILLIS, TimeUnit.MILLISECONDS))
                .isInstanceOf(TimeoutException.class);
    }

    private void assertClosedWithMessageCount(int expectedCount) {
        assertThat(
                conversationRepository.findByIdOrThrow(conversationId)
                        .getStatus()
        ).isEqualTo(AssistantConversationStatus.CLOSED);
        assertThat(
                messageRepository.findAllByConversationIdOrderByIdDesc(conversationId, PageRequest.of(0, 10))
                        .getContent()
        ).hasSize(expectedCount);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("latch timeout");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread()
                    .interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
