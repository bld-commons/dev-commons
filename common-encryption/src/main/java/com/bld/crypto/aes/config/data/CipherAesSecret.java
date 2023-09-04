/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.data.CipherAesSecret.java
 */
package com.bld.crypto.aes.config.data;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.commons.collections4.MapUtils;

/**
 * The Class CipherAesSecret.
 */
public class CipherAesSecret {

	/** The map. */
	private Map<String,SecretKey> map;
	


	/**
	 * Instantiates a new cipher aes secret.
	 */
	public CipherAesSecret() {
		super();
		this.map=new HashMap<>();
	}



	/**
	 * Adds the secret key.
	 *
	 * @param name the name
	 * @param secretKey the secret key
	 */
	public void addSecretKey(String name,final SecretKey secretKey) {
		this.map.put(name, secretKey);
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
	
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return MapUtils.isEmpty(this.map);
	}
}
