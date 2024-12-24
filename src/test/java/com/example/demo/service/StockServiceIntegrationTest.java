package com.example.demo.service;

import com.example.demo.entity.Stock;
import com.example.demo.repository.StockRepository;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//Testcontainers require Docker to run containers like redis and mysql
// so ensure docker is running in local
@Ignore
@Testcontainers
@SpringBootTest
public class StockServiceIntegrationTest {

        @Container
        private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("test_db")
                .withUsername("user")
                .withPassword("password");

        @Container
        private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:6.0")
                .withExposedPorts(6379);

        @DynamicPropertySource
        static void dynamicProperties(DynamicPropertyRegistry registry) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
            registry.add("spring.datasource.username", mysqlContainer::getUsername);
            registry.add("spring.datasource.password", mysqlContainer::getPassword);
            registry.add("spring.redis.host", redisContainer::getHost);
            registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
        }

        @Autowired
        private StockService stockService;

        @Autowired
        private StockRepository stockRepository;

        @Test
        void testCreateAndRetrieveStock() {
            Stock newStock = new Stock();
            newStock.setName("Integration Test Stock");
            newStock.setQuantity(100);
            newStock.setPrice(25.0);

            CompletableFuture<Stock> createdStockFuture = stockService.createStock(newStock);
            Stock createdStock = createdStockFuture.join();

            CompletableFuture<Stock> retrievedStockFuture = stockService.getStockById(createdStock.getId());
            Stock retrievedStock = retrievedStockFuture.join();

            assertNotNull(retrievedStock);
            assertEquals("Integration Test Stock", retrievedStock.getName());
        }
}
