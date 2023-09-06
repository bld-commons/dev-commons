/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.config.data.CipherPublicKeys.java
 */
package com.bld.crypto.pubkey.config.data;

import java.security.PublicKey;
import java.util.Map;


/**
 * The Class CipherPublicKeys.
 */
public class CipherPublicKeys {

	/** The map. */
	private final Map<String,PublicKey> map;
	


	/**
	 * Instantiates a new cipher public keys.
	 *
	 * @param map the map
	 */
	public CipherPublicKeys(Map<String,PublicKey> map) {
		super();
		this.map=map;
	}



	
	/**
	 * Gets the public key.
	 *
	 * @param name the name
	 * @return the public key
	 */
	public PublicKey getPublicKey(String name) {
		return this.map.get(name);
	}
	
	
	
	
}
