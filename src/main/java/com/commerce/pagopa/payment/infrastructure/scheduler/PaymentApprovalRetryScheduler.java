package com.commerce.pagopa.payment.infrastructure.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.payment.event.PaymentApprovalRequested;

@Component
@RequiredArgsConstructor
public class PaymentApprovalRetryScheduler {

    private final FailedEventPublications failedEventPublications;

    @Scheduled(fixedDelayString = "${app.payment.approval-retry-delay:30000}")
    void retry() {
        Instant idempotencyCutoff = Instant.now().minus(Duration.ofDays(14));

        failedEventPublications.resubmit(
                ResubmissionOptions.defaults()
                        .withBatchSize(20)
                        .withMaxInFlight(5)
                        .withMinAge(Duration.ofSeconds(30))
                        .withFilter(publication ->
                                        canProcess(
                                                publication,
                                                idempotencyCutoff
                                        )
                        )
        );
    }

    private static boolean canProcess(
            EventPublication publication,
            Instant idempotencyCutoff
    ) {
        return publication.getEvent() instanceof PaymentApprovalRequested
                && publication.getCompletionAttempts() < 10
                && publication.getPublicationDate().isAfter(idempotencyCutoff);
    }
}
