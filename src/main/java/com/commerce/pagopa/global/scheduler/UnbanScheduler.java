package com.commerce.pagopa.global.scheduler;

import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.entity.enums.UserStatus;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UnbanScheduler {

    private final UserRepository userRepository;

    /**
     * 매 분 실행
     */
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void unbanSchedule() {
        List<User> unbanTargets = userRepository.findByBanEndDateIsBeforeAndUserStatus(
                LocalDateTime.now(),
                UserStatus.BANNED
        );
        for (User target : unbanTargets) {
            target.unban();
        }
    }
}
