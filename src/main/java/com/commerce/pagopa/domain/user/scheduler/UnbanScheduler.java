package com.commerce.pagopa.domain.user.scheduler;

import com.commerce.pagopa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    public void unbanSchedule() {
        log.info("[UnbanScheduler] 정지 해제 스케줄링 시작");
        userRepository.bulkUnbanBefore(LocalDateTime.now());
    }
}
