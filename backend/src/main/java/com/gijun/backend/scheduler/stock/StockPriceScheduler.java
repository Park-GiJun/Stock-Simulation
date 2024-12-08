package com.gijun.backend.scheduler.stock;

import com.gijun.backend.service.StockPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Configuration
@EnableScheduling
@Slf4j
public class StockPriceScheduler {

    private final StockPriceService stockPriceService;
    private final Clock clock;

    public StockPriceScheduler(StockPriceService stockPriceService, Clock clock) {
        this.stockPriceService = stockPriceService;
        this.clock = clock;
    }

    @Scheduled(cron = "*/30 * 9-15 * * MON-SUN") // 평일 9시-15시 사이 30초마다 실행
    public void executeStockPriceUpdate() {
        LocalTime currentTime = LocalDateTime.now(clock).toLocalTime();
        if (currentTime.isBefore(LocalTime.of(9, 0)) || currentTime.isAfter(LocalTime.of(15, 30))) {
            return;
        }

        log.info("Starting stock price update batch at {}", LocalDateTime.now(clock));
        stockPriceService.updateAllStockPrices();
    }
}