/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.config.data.PublicKeyProperties.java
 */
package com.bld.crypto.pubkey.config.data;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;


/**
 * The Class PublicKeyProperties.
 */
@Component
@ConfigurationProperties(prefix = "com.bld.crypto.public-key",ignoreUnknownFields = true)
public final class PublicKeyProperties {

	/** The keys. */
	private final Map<String,Resource> keys;
	
	

	/**
	 * Instantiates a new public key properties.
	 *
	 * @param keys the keys
	 */
	public PublicKeyProperties(Map<String, Resource> keys) {
		super();
		this.keys = keys;
	}


	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	public Map<String, Resource> getKeys() {
		return keys;
	}

	

	
	
	
}
