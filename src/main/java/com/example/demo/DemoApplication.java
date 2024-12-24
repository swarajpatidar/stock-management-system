package com.example.demo;

import com.example.demo.service.BenchmarkService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.example.demo")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

//	/**
//	 * Trigger benchmarking call internally
//	 * @param benchmarkService
//	 * @return
//	 */
//	@Bean
//	public ApplicationRunner benchmarkRunner(BenchmarkService benchmarkService) {
//		return args -> {
//			benchmarkService.runBenchmark();
//		};
//	}

}
