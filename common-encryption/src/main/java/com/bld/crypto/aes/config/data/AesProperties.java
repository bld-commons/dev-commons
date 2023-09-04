/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.config.data.AesProperties.java
 */
package com.bld.crypto.aes.config.data;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



/**
 * The Class AesProperties.
 */
@Component
@ConfigurationProperties(prefix = "com.bld.crypto.aes",ignoreUnknownFields = true)
public final class AesProperties {

	/** The keys. */
	private final Map<String,Aes> keys;
	
	

	/**
	 * Instantiates a new aes properties.
	 *
	 * @param keys the keys
	 */
	public AesProperties(Map<String, Aes> keys) {
		super();
		this.keys = keys;
	}



	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	public Map<String, Aes> getKeys() {
		return keys;
	}


	
	
	
}
