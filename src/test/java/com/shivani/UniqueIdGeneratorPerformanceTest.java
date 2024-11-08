package com.shivani;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniqueIdGeneratorPerformanceTest {
	
	private static final Logger logger = LoggerFactory.getLogger(UniqueIdGeneratorPerformanceTest.class);

	@Test
	public void testIdGenerationWithSingleThread() {
		int iterations = 1000000; // 1 million
		UniqueIdGenerator idGenerator = new UniqueIdGenerator(897);

		// Measure start time using nanoTime for higher precision
		long beginTimestamp = System.nanoTime();

		// Generate IDs
		for (int i = 0; i < iterations; i++) {
			idGenerator.createNewId();
		}

		// Measure end time
		long endTimestamp = System.nanoTime();

		// Calculate duration and IDs per millisecond
		long durationInMs = (endTimestamp - beginTimestamp) / 1_000_000; // Convert from nanoseconds to milliseconds
		double idsPerMs = (double) iterations / durationInMs; // Using double to avoid truncation
		logger.info("Single Thread:: IDs generated: {} | Time taken: {} ms | IDs per ms: {}", iterations, durationInMs,
				idsPerMs);
	}

	@Test
    public void testIdGenerationWithMultipleThreads() throws InterruptedException {
        int iterations = 1000000; // 1 million
        int numThreads = 50;

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(iterations);

        UniqueIdGenerator idGenerator = new UniqueIdGenerator(897);

        // Use nanoTime for better precision
        long beginTimestamp = System.nanoTime();

        // Submit tasks in a batch using a loop
        for (int i = 0; i < iterations; i++) {
            executorService.submit(() -> {
                idGenerator.createNewId();
                latch.countDown();
            });
        }

        // Wait for all tasks to complete
        latch.await();

        // Calculate elapsed time in milliseconds
        long endTimestamp = System.nanoTime();
        long durationInMs = (endTimestamp - beginTimestamp) / 1_000_000; // Convert to milliseconds

        // Calculate the number of IDs generated per millisecond
        double idsPerMs = (double) iterations / durationInMs; // Using double to avoid truncation

        // Log the result after all tasks are finished
        logger.info("{} Threads:: IDs per ms: {}", numThreads, idsPerMs);

        // Gracefully shut down the executor service
        executorService.shutdown();
	}
}
