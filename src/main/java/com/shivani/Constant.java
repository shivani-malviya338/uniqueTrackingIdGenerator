package com.shivani;

public class Constant {

	// 41 bits reserved for timestamp bits.
	public static final int TIMESTAMP_BITS = 41;

	// 5 bits reserved for dataCeneterId bits.
	public static final int DATA_CENTER_ID_BITS = 5;

	// 5 bits reserved for machineId bits.
	public static final int MACHINE_ID_BITS = 5;

	// 12 bits for sequence number. This means that we can generate 2^12 = 4096 in
	// one
	// milliseconds on each server.
	public static final int SEQUENC_NUMBER = 12;

	// Custom Epoch (Fri Nov 08 2024 01:35:54 GMT+0530 (India Standard Time))
	public static final long DEFAULT_EPOCH = 1731053066L;

	public static final String SUFFIX = "TD";
	public static final String PREFIX = "IND";
}