package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Profile("local")
@Component
@RequiredArgsConstructor
class SeedRunner implements ApplicationRunner {

    private final SeedProperties props;
    private final List<Seeder> seeders;

    @Override
    public void run(ApplicationArguments args) {
        if (!props.enabled()) {
            log.info("[seed] disabled (app.seed.enabled=false)");
            return;
        }
        log.info("[seed] start - {} seeders registered", seeders.size());
        long total = System.currentTimeMillis();

        for (Seeder seeder : seeders) {
            if (!seeder.shouldRun()) {
                log.info("[seed] skip {} (already seeded)", seeder.name());
                continue;
            }
            long t = System.currentTimeMillis();
            log.info("[seed] >>> {}", seeder.name());
            try {
                seeder.seed();
                log.info("[seed] <<< {} ({}ms)", seeder.name(), System.currentTimeMillis() - t);
            } catch (Exception e) {
                log.error("[seed] failed {} after {}ms", seeder.name(), System.currentTimeMillis() - t, e);
                throw new RuntimeException("Seeding failed for: " + seeder.name(), e);
            }

        }
        log.info("[seed] done ({}ms)", System.currentTimeMillis() - total);
    }
}
