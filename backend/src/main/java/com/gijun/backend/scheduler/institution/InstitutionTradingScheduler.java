package com.gijun.backend.scheduler.institution;

import com.gijun.backend.service.InstitutionTradingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
@EnableScheduling
@AllArgsConstructor
@Slf4j
public class InstitutionTradingScheduler {

    private final InstitutionTradingService tradingService;
    private final Clock clock;


    @Scheduled(cron = "0 */1 9-15 * * MON-SUN") // 평일 9시-15시 사이 1분마다 실행
    public void executeInstitutionTrading() {
        LocalTime currentTime = LocalDateTime.now(clock).toLocalTime();
        if (currentTime.isBefore(LocalTime.of(9, 0)) || currentTime.isAfter(LocalTime.of(15, 30))) {
            return;
        }

        log.info("Starting institution trading batch at {}", LocalDateTime.now(clock));
        tradingService.executeBatchTrading();
    }
}
