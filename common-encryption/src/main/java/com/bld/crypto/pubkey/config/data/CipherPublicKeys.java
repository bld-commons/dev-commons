/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.config.data.CipherPublicKeys.java
 */
package com.bld.crypto.pubkey.config.data;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import com.bld.crypto.commons.KeyUtility;
import com.bld.crypto.type.InstanceType;


/**
 * The Class CipherPublicKeys.
 */
public class CipherPublicKeys {

	/** The map. */
	private final Map<String,PublicKey> map;
	


	/**
	 * Instantiates a new cipher public keys.
	 *
	 */
	public CipherPublicKeys() {
		super();
		this.map=new HashMap<>();
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
	
	public void addPublicKey(String key,PublicKey publicKey) {
		this.map.put(key, publicKey);
		
	}
	
	public void addPublicKey(String key,String publicKey,InstanceType instanceType) {
		PublicKey pk=KeyUtility.getPublicKey(publicKey,instanceType);
		this.addPublicKey(publicKey, pk);
	}
	
}
