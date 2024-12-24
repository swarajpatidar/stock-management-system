package com.example.demo.Logger;

import com.example.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheMetricsLogger {

    private final StockService stockService;

    @Autowired
    public CacheMetricsLogger(StockService stockService) {
        this.stockService = stockService;
    }

    @Scheduled(fixedRate = 60000) // Log every 60 seconds
    public void logMetrics() {
        stockService.logCacheMetrics();
    }
}
