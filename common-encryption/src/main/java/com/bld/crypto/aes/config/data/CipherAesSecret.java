/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.data.CipherAesSecret.java
 */
package com.bld.crypto.aes.config.data;

import java.util.Map;

import javax.crypto.SecretKey;

/**
 * The Class CipherAesSecret.
 */
public class CipherAesSecret {

	/** The map. */
	private final Map<String,SecretKey> map;
	

	
	/**
	 * Instantiates a new cipher aes secret.
	 *
	 * @param map the map
	 */
	public CipherAesSecret(Map<String,SecretKey>map) {
		super();
		this.map=map;
	}



	/**
	 * Gets the secret key.
	 *
	 * @param name the name
	 * @return the secret key
	 */
	public SecretKey getSecretKey(String name) {
		return this.map.get(name);
	}
	
	
}
