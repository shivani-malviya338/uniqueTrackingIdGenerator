package com.shivani;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the UniqueIdGenerator class.
 * 
 * These tests verify the functionality of the UniqueIdGenerator, ensuring that:
 * - IDs are generated correctly with the expected structure. - The generator
 * produces unique IDs even when called concurrently from multiple threads. -
 * The ID generation respects the configured timestamp, server ID, and sequence
 * number bits.
 * 
 */
public class UniqueIdGeneratorTest {

	@Test
	public void shouldGenerateValidIdWithCorrectParts() {
		UniqueIdGenerator idGenerator = new UniqueIdGenerator(784);

		// Record the time before generating the ID
		long beforeTimestamp = Instant.now().toEpochMilli();

		// Generate the ID and extract the numeric part
		String id = idGenerator.createNewId();
		long idNumber = Long.parseLong(id.replaceAll("[^0-9]", "")); // Directly parse the numeric value from the ID

		// Fetch the parts of the generated ID
		long[] parts = idGenerator.extractIdComponents(idNumber);

		// Assertions
		assertTrue(parts[0] >= beforeTimestamp, "Timestamp part is incorrect.");
		assertEquals(784, parts[1], "Server ID part is incorrect.");
		assertEquals(0, parts[2], "Sequence part should start from 0.");
	}

	@Test
	public void shouldGenerateUniqueId() {
		UniqueIdGenerator idGenerator = new UniqueIdGenerator(234);
		int iterations = 5000;

		// Validate that the IDs are unique even if they are generated in the same
		// millisecond
		Set<String> generatedIds = new HashSet<>();
		for (int i = 0; i < iterations; i++) {
			String id = idGenerator.createNewId();
			assertTrue(generatedIds.add(id), "Duplicate ID found: " + id); // `add` returns false if duplicate
		}

		// Assert that the number of generated IDs matches the number of unique IDs
		assertEquals(iterations, generatedIds.size(), "Not all IDs are unique.");
	}

	@Test
	public void shouldGenerateUniqueIdIfCalledFromMultipleThreads() throws InterruptedException, ExecutionException {
		int numThreads = 20;
		ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
		int iterations = 50000;
		CountDownLatch latch = new CountDownLatch(iterations);

		UniqueIdGenerator idGenerator = new UniqueIdGenerator(234);

		// Validate that the IDs are unique even if they are generated in the same
		// millisecond in different threads
		Set<String> generatedIds = ConcurrentHashMap.newKeySet(iterations); // Thread-safe set

		List<Callable<Void>> tasks = new ArrayList<>();
		for (int i = 0; i < iterations; i++) {
			tasks.add(() -> {
				String id = idGenerator.createNewId();
				generatedIds.add(id); // Add ID to the set
				latch.countDown();
				return null;
			});
		}

		executorService.invokeAll(tasks); // Execute all tasks
		latch.await();

		// Assert that the number of generated IDs matches the number of unique IDs
		assertEquals(iterations, generatedIds.size(), "Not all IDs are unique.");
		executorService.shutdown();
	}
}
