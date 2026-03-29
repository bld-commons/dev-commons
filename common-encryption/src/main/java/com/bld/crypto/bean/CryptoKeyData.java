/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.bean.CryptoKeyData.java
 */
package com.bld.crypto.bean;


/**
 * Data transfer object that carries the key name and the URL-encoding flag used by
 * serializers and deserializers to locate the correct cryptographic key and to decide
 * whether the encrypted payload must be further Base64-encoded for safe embedding in
 * a URL.
 */
public class CryptoKeyData {

	/** The logical name of the cryptographic key, used to look it up in the key store. */
	private String name;

	/** Whether the encrypted value must be URL-safe Base64-encoded. */
	private boolean url;

	/**
	 * Constructs a new {@code CryptoKeyData} instance.
	 *
	 * @param name the logical name of the cryptographic key
	 * @param url  {@code true} if the encrypted value should be URL-safe Base64-encoded
	 */
	public CryptoKeyData(String name, boolean url) {
		super();
		this.name = name;
		this.url = url;
	}

	/**
	 * Returns the logical name of the cryptographic key.
	 *
	 * @return the key name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns whether the encrypted value must be URL-safe Base64-encoded.
	 *
	 * @return {@code true} if URL-encoding is required, {@code false} otherwise
	 */
	public boolean isUrl() {
		return url;
	}
	
	
	
}
