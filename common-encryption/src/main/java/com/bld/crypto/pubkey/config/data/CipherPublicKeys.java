/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.config.data.CipherPublicKeys.java
 */
package com.bld.crypto.pubkey.config.data;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;


/**
 * The Class CipherPublicKeys.
 */
public class CipherPublicKeys {

	/** The map. */
	private Map<String,PublicKey> map;
	

	/**
	 * Instantiates a new cipher public keys.
	 */
	public CipherPublicKeys() {
		super();
		this.map=new HashMap<>();
	}


	/**
	 * Adds the public key.
	 *
	 * @param name the name
	 * @param publicKey the public key
	 */
	public void addPublicKey(String name,final PublicKey publicKey) {
		this.map.put(name, publicKey);
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
	
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return MapUtils.isEmpty(this.map);
	}
	
	
}
