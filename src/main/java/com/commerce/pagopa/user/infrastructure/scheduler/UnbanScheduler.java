package com.commerce.pagopa.user.infrastructure.scheduler;

import com.commerce.pagopa.user.domain.model.enums.UserStatus;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnbanScheduler {

    private final UserRepository userRepository;

    /**
     * TODO: 다중 인스턴스 실행 시 단일 실행 보장
     * 매 분 실행
     */
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void unSuspendSchedule() {
        log.info("[unSuspendSchedule] 임시 정지 해제 스케줄링 시작");
        Instant now = Instant.now();
        Instant threshold = now.atZone(ZoneId.of("Asia/Seoul"))
                .minusDays(7)
                .toInstant();

        int unSuspendCount = userRepository.bulkUnSuspend(
                UserStatus.ACTIVE,
                UserStatus.SUSPENDED,
                now,
                threshold
        );
		log.info("[unSuspendSchedule] 임시 정지 해제 회원 수: {}", unSuspendCount);
    }
}
