package com.example.demo.service;

import com.example.demo.entity.Stock;
import com.example.demo.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockServiceTest {

        @Mock
        private StockRepository stockRepository;

        @Mock
        private RedisTemplate<String, Object> redisTemplate;

        @Mock
        private ValueOperations<String, Object> valueOperations;

        @InjectMocks
        private StockService stockService;

        private final ExecutorService executorService = Executors.newFixedThreadPool(2);

        @BeforeEach
        void setUp() {
            stockService = new StockService(stockRepository, redisTemplate, executorService);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void testGetStockById_CacheHit() {
            Long stockId = 1L;
            Stock cachedStock = new Stock();
            cachedStock.setId(stockId);
            cachedStock.setName("Test Stock");
            cachedStock.setQuantity(100);
            cachedStock.setPrice(50.0);

            when(valueOperations.get("stock_" + stockId)).thenReturn(cachedStock);
            CompletableFuture<Stock> stockFuture = stockService.getStockById(stockId);
            Stock result = stockFuture.join();
            assertNotNull(result);
            assertEquals("Test Stock", result.getName());
            verify(valueOperations, times(1)).get("stock_" + stockId);
            verifyNoMoreInteractions(stockRepository);
        }

        @Test
        void testGetStockById_CacheMiss() {
            Long stockId = 2L;
            Stock stockFromDb = new Stock();
            stockFromDb.setId(stockId);
            stockFromDb.setName("New Stock");
            stockFromDb.setQuantity(50);
            stockFromDb.setPrice(100.0);
            when(valueOperations.get("stock_" + stockId)).thenReturn(null);
            when(stockRepository.findById(stockId)).thenReturn(Optional.of(stockFromDb));
            CompletableFuture<Stock> stockFuture = stockService.getStockById(stockId);
            Stock result = stockFuture.join();
            assertNotNull(result);
            assertEquals("New Stock", result.getName());
            verify(valueOperations, times(1)).get("stock_" + stockId);
            verify(stockRepository, times(1)).findById(stockId);
            verify(valueOperations, times(1)).set("stock_" + stockId, stockFromDb, Duration.ofHours(1));
        }

}
