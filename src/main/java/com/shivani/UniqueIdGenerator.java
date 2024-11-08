package com.shivani;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * UniqueIdGenerator is a distributed sequence generator. It uses
 * snowflake algorithm to achieve concurrency, fault tolerance, and scalability.
 */
public class UniqueIdGenerator {

	private static final Logger logger = LoggerFactory.getLogger(UniqueIdGenerator.class);

	private static final long MAX_SERVER_ID = (1L << Constant.DATA_CENTER_ID_BITS + Constant.MACHINE_ID_BITS) - 1;
	private static final long MAX_SEQUENCE = (1L << Constant.SEQUENC_NUMBER) - 1;

	private final long serverId;
	private final long customEpoch;

	private volatile long lastTimestamp = -1L;
	private volatile long counter = 0L;

	/**
	 * Creates a new generator instance with a specific server ID and custom epoch.
	 *
	 * @param serverId    unique identifier for the server instance.
	 * @param customEpoch the base epoch timestamp to be used for ID generation.
	 */
	public UniqueIdGenerator(long serverId, long customEpoch) {
		if (serverId < 0 || serverId > MAX_SERVER_ID) {
			throw new IllegalArgumentException(String.format("ServerId must be between %d and %d", 0, MAX_SERVER_ID));
		}
		this.serverId = serverId;
		this.customEpoch = customEpoch;
	}

	/**
	 * Creates a new generator instance with a specific server ID and custom epoch.
	 *
	 * @param serverId unique identifier for the server instance.
	 */
	public UniqueIdGenerator(long serverId) {
		this(serverId, Constant.DEFAULT_EPOCH);
	}

	/**
	 * Produces the next sequential unique identifier.
	 *
	 * @return a unique ID.
	 */
	public synchronized String createNewId() {
		long currentTimestamp = getCurrentTimestamp();

		// Ensure the current timestamp is valid
		if (currentTimestamp < lastTimestamp) {
			logger.error("Invalid System Clock detected!");
			throw new IllegalStateException("Invalid System Clock!");
		}

		// If in the same millisecond, increment the counter
		if (currentTimestamp == lastTimestamp) {
			counter = (counter + 1) & MAX_SEQUENCE; // Use the counter mask to prevent overflow
			if (counter == 0) { // If counter overflows, wait for the next millisecond
				currentTimestamp = waitUntilNextMillis(currentTimestamp);
			}
		} else {
			// If timestamp is different, reset the counter
			counter = 0;
		}
		// Update the lastTimestamp to the current one
		lastTimestamp = currentTimestamp;

		// Generate the unique ID using bit shifts
		long id = (currentTimestamp << (Constant.DATA_CENTER_ID_BITS + Constant.MACHINE_ID_BITS
				+ Constant.SEQUENC_NUMBER)) | (serverId << Constant.SEQUENC_NUMBER) | counter;

		return Constant.PREFIX + id + Constant.SUFFIX;
	}

	private long getCurrentTimestamp() {
		return Instant.now().toEpochMilli() - customEpoch;
	}

	/**
	 * Pauses execution until the next millisecond.
	 *
	 * @param currentTimestamp the current timestamp.
	 * @return the timestamp after the wait.
	 */
	private long waitUntilNextMillis(long currentTimestamp) {
		while (currentTimestamp == lastTimestamp) {
			currentTimestamp = getCurrentTimestamp();
		}
		return currentTimestamp;
	}

	/**
	 * Extracts the timestamp, server ID, and sequence from a generated ID.
	 *
	 * @param id the generated unique identifier.
	 * @return an array with the timestamp, server ID, and sequence.
	 */
	public long[] extractIdComponents(long id) {
		// Corrected the mask calculation with parentheses for proper precedence
		long maskServerId = ((1L << Constant.DATA_CENTER_ID_BITS + Constant.MACHINE_ID_BITS)
				- 1) << Constant.SEQUENC_NUMBER;
		long maskCounter = (1L << Constant.SEQUENC_NUMBER) - 1;

		// Extract the parts of the ID
		long timestamp = (id >> (Constant.DATA_CENTER_ID_BITS + Constant.MACHINE_ID_BITS + Constant.SEQUENC_NUMBER))
				+ customEpoch;
		long serverId = (id & maskServerId) >> Constant.SEQUENC_NUMBER;
		long sequence = id & maskCounter;

		// Return the timestamp, serverId, and sequence in an array
		return new long[] { timestamp, serverId, sequence };
	}

	@Override
	public String toString() {
		return "UniqueIdGenerator Settings [TIMESTAMP_BITS=" + Constant.TIMESTAMP_BITS + ", DATA_CENTER_ID_BITS ="
				+ Constant.DATA_CENTER_ID_BITS + ", MACHINE_ID_BITS =" + Constant.MACHINE_ID_BITS + ", COUNTER_BITS="
				+ Constant.SEQUENC_NUMBER + ", CUSTOM_EPOCH=" + customEpoch + ", ServerId=" + serverId + "]";
	}

	public static void main(String[] args) {
		UniqueIdGenerator idGenerator = new UniqueIdGenerator(455);
		for (int i = 0; i < 10; i++) {
			System.out.println(idGenerator.createNewId());
		}
	}
}
