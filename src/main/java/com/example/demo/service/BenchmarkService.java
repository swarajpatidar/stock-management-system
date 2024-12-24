package com.example.demo.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import com.example.demo.entity.Stock;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

    @Service
    public class BenchmarkService {

        private final StockService stockService;

        private static final Logger logger = LoggerFactory.getLogger(BenchmarkService.class);

        @Autowired
        public BenchmarkService(StockService stockService) {
            this.stockService = stockService;
        }

        public void runBenchmark() {
            runSequentialExecution();
            runParallelExecution();
        }

        private void runSequentialExecution() {
            StopWatch sequentialWatch = new StopWatch("Sequential Execution");
            sequentialWatch.start();

            for (int i = 1; i <= 6; i++) {
                stockService.getStockById((long) i).join();
            }

            sequentialWatch.stop();
            logger.info(sequentialWatch.prettyPrint());
        }

        private void runParallelExecution() {
            StopWatch parallelWatch = new StopWatch("Parallel Execution");
            parallelWatch.start();

            List<CompletableFuture<Stock>> futures = IntStream.rangeClosed(1, 6)
                    .mapToObj(i -> stockService.getStockById((long) i))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            parallelWatch.stop();
            logger.info(parallelWatch.prettyPrint());
        }
}
