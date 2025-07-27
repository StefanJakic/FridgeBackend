package com.hylastix.fridge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpiringItemsBatchService {

    private final FridgeService fridgeService;

    // Run every day at 5:00 AM
    @Scheduled(cron = "0 0 5 * * ?")
    public void checkExpiringItemsDaily() {
        log.info("Starting daily expiring items check...");
        try {
            fridgeService.checkAndNotifyExpiringItems();
            log.info("Daily expiring items check completed successfully");
        } catch (Exception e) {
            log.error("Error during daily expiring items check: {}", e.getMessage(), e);
        }
    }
} 