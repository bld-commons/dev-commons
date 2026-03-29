package com.bld.crypto.type;

/**
 * Enumeration of the valid AES key sizes in bytes.
 *
 * <p>AES supports three key lengths:
 * <ul>
 *   <li>{@link #sz16} – 128-bit key (16 bytes)</li>
 *   <li>{@link #sz24} – 192-bit key (24 bytes)</li>
 *   <li>{@link #sz32} – 256-bit key (32 bytes)</li>
 * </ul>
 */
public enum AesSizeType {

	/** 128-bit AES key (16 bytes). */
	sz16(16),
	/** 192-bit AES key (24 bytes). */
	sz24(24),
	/** 256-bit AES key (32 bytes). */
	sz32(32);

	/** The key size in bytes. */
	private int size;

	/**
	 * Constructs an {@code AesSizeType} with the given byte size.
	 *
	 * @param size the key length in bytes
	 */
	private AesSizeType(int size) {
		this.size=size;
	}

	/**
	 * Returns the key size in bytes.
	 *
	 * @return the number of bytes in this AES key size
	 */
	public int size() {
		return this.size;
	}
}
