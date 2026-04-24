package com.commerce.pagopa.global.scheduler;

import com.commerce.pagopa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        userRepository.bulkUnbanBefore(LocalDateTime.now());
    }
}
