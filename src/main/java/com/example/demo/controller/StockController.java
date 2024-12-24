package com.example.demo.controller;

import com.example.demo.entity.Stock;
import com.example.demo.service.BenchmarkService;
import com.example.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/stocks")
public class StockController {
    @Autowired
    private final StockService stockService;
    @Autowired
    private final BenchmarkService benchmarkService;

    public StockController(StockService stockService, BenchmarkService benchmarkService) {
        this.stockService = stockService;
        this.benchmarkService = benchmarkService;
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<Stock>> getStock(@PathVariable Long id) {
        return stockService.getStockById(id)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/create")
    public CompletableFuture<ResponseEntity<Stock>> createStock(@RequestBody Stock stock) {
        return stockService.createStock(stock)
                .thenApply(savedStock -> ResponseEntity.ok(savedStock));
    }

    @PutMapping("/update")
    public CompletableFuture<ResponseEntity<Stock>> updateStock(@RequestBody Stock stock) {
        return stockService.updateStock(stock)
                .thenApply(updatedStock -> ResponseEntity.ok(updatedStock));
    }

    // Explicitly trigger benchmarking
    /**
     * Trigger benchmarking
     * @return
     */
    @GetMapping("/benchmark")
    public ResponseEntity<String> runBenchmark() {
        benchmarkService.runBenchmark();
        return ResponseEntity.ok("Benchmark executed. Check logs for results.");
    }

}
