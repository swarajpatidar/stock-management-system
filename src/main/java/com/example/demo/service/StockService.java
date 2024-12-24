package com.example.demo.service;

import com.example.demo.DemoApplication;
import com.example.demo.entity.Stock;
import com.example.demo.repository.StockRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ExecutorService executorService;

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final AtomicInteger cacheHit = new AtomicInteger(0);
    private final AtomicInteger cacheMiss = new AtomicInteger(0);

    @Autowired
    public StockService(StockRepository stockRepository, RedisTemplate<String, Object> redisTemplate,
                        ExecutorService executorService) {
        this.stockRepository = stockRepository;
        this.redisTemplate = redisTemplate;
        this.executorService = executorService;
    }

    /**
     * fetch a stock.
     */
    public CompletableFuture<Stock> getStockById(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            try {
                String cacheKey = "stock_" + id;
                Stock cachedStock = (Stock) redisTemplate.opsForValue().get(cacheKey);

                if (cachedStock != null) {
                    cacheHit.incrementAndGet();
                    logger.info("Cache hit for key: {}", cacheKey);
                    return cachedStock;
                }
                cacheMiss.incrementAndGet();
                logger.info("Cache miss for key: {}", cacheKey);
                Stock stock = stockRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Stock not found"));
                redisTemplate.delete(cacheKey);
//                redisTemplate.opsForValue().set(cacheKey, stock);
//                cache for 1 hour
                redisTemplate.opsForValue().set(cacheKey, stock, Duration.ofHours(1));
                return stock;
            } finally {
                stopWatch.stop();
                logger.info("Task executed in {} ms", stopWatch.getTotalTimeMillis());
            }
        }, executorService);
    }

    /**
     * create a stock.
     */
    @Transactional
    @Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public CompletableFuture<Stock> createStock(Stock stock) {
        return CompletableFuture.supplyAsync(() -> {
            Stock newStock = new Stock();
            newStock.setName(stock.getName());
            newStock.setQuantity(stock.getQuantity());
            newStock.setPrice(stock.getPrice());
//            newStock.setVersion(0);
            Stock savedStock = stockRepository.save(newStock);

            // Cache the new stock
            String cacheKey = "stock_" + savedStock.getId();
            redisTemplate.delete(cacheKey);
//            redisTemplate.opsForValue().set(cacheKey, savedStock);
//            cache for 1 hour
            redisTemplate.opsForValue().set(cacheKey, savedStock, Duration.ofHours(1));

            return savedStock;
        }, executorService);
    }


    /**
     * Update an existing stock.
     */
//    common endpoint for both create and update stock using both optimistic and pessimistic lock
//    @Transactional
//    @Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
//    public CompletableFuture<Stock> updateStock(Stock stock) {
//        return CompletableFuture.supplyAsync(() -> {
//            logger.info("Updating stock with ID: {}", stock.getId());
//            // Find the existing stock or create a new one
//            Stock existingStock = stockRepository.findByIdWithLock(stock.getId())
//                    .orElse(null);
//
//            if (existingStock == null) {
//                logger.info("Creating new stock entry for ID: {}", stock.getId());
//                existingStock = new Stock();
//                existingStock.setVersion(0); // Initialize version for new record
//            }
//
//            // Update fields
//            existingStock.setName(stock.getName());
//            existingStock.setQuantity(stock.getQuantity());
//            existingStock.setPrice(stock.getPrice());
//
//            // Save the updated stock
//            try {
//                Stock updatedStock = stockRepository.save(existingStock);
//
//                // Optionally cache the updated stock
//                String cacheKey = "stock_" + updatedStock.getId();
//                redisTemplate.opsForValue().set(cacheKey, updatedStock);
//
//                return updatedStock; // Return the updated stock
//            } catch (ObjectOptimisticLockingFailureException e) {
//                logger.warn("Optimistic locking failure, retrying...");
//                throw e; // Will trigger a retry
//            }
//        }, executorService);
//    }

    @Transactional
    @Retryable(value = ObjectOptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public CompletableFuture<Stock> updateStock(Stock stock) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Updating stock with ID: {}", stock.getId());

            // Find the existing stock with pessimistic lock
            Stock existingStock = stockRepository.findByIdWithLock(stock.getId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            // Update fields
            existingStock.setName(stock.getName());
            existingStock.setQuantity(stock.getQuantity());
            existingStock.setPrice(stock.getPrice());

            // Save the updated stock
            Stock updatedStock = stockRepository.save(existingStock);

            // Optionally cache the updated stock
            String cacheKey = "stock_" + updatedStock.getId();
            redisTemplate.delete(cacheKey);
//            redisTemplate.opsForValue().set(cacheKey, updatedStock);
//            cache for 1 hour
            redisTemplate.opsForValue().set(cacheKey, updatedStock, Duration.ofHours(1));

            return updatedStock;
        }, executorService);
    }

    /**
    *   Log cache metrics
    */
    public void logCacheMetrics() {
        logger.info("Cache Metrics - Hits: {}, Misses: {}",
                cacheHit.get(), cacheMiss.get());
    }
}
