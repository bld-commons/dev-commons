/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.CryptoMapPublicKeyUtils.java
 */
package com.bld.crypto.pubkey;

import com.bld.crypto.bean.CryptoKeyUtils;
import com.bld.crypto.pubkey.config.data.CipherPublicKeys;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;


/**
 * The Class CryptoMapPublicKeyUtils.
 */
public class CryptoMapPublicKeyUtils extends CryptoKeyUtils {

	/** The cipher public keys. */
	private final CipherPublicKeys cipherPublicKeys;
	
	

	public CryptoMapPublicKeyUtils(CipherPublicKeys cipherPublicKeys) {
		super();
		this.cipherPublicKeys = cipherPublicKeys;
	}

	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String encryptValue(String value,final String key) {
		return super.encryptValue(value, cipherPublicKeys.getPublicKey(key),InstanceType.RSA);
	}
	
	/**
	 * Encrypt object.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObject(Object value,final String key) throws JsonProcessingException {
		return this.encryptValue(this.objMapper.writeValueAsString(value), key);
	}
	
	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String decryptValue(String value,final String key) {
		return super.decryptValue(value, cipherPublicKeys.getPublicKey(key),InstanceType.RSA);
	}

	/**
	 * Decrypt object.
	 *
	 * @param <T> the generic type
	 * @param value the value
	 * @param response the response
	 * @param key the key
	 * @return the t
	 * @throws JsonMappingException the json mapping exception
	 * @throws JsonProcessingException the json processing exception
	 */
	public <T>T decryptObject(String value,Class<T> response,final String key) throws JsonMappingException, JsonProcessingException {
		String json=this.decryptValue(value, key);
		return this.objMapper.readValue(json, response);
	}

	/**
	 * Encrypt uri.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String encryptUri(String value,final String key) {
		return super.encryptUri(value, key);
	}
	
	/**
	 * Encrypt object uri.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObjectUri(Object value,final String key) throws JsonProcessingException {
		return this.encryptUri(this.objMapper.writeValueAsString(value), key);
	}
	
	/**
	 * Decrypt uri.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String decryptUri(String value,final String key) {
		return super.decryptUri(value, key);
	}
	
	/**
	 * Decrypt object uri.
	 *
	 * @param <T> the generic type
	 * @param value the value
	 * @param response the response
	 * @param key the key
	 * @return the t
	 * @throws JsonMappingException the json mapping exception
	 * @throws JsonProcessingException the json processing exception
	 */
	public <T>T decryptObjectUri(String value,Class<T>response,final String key) throws JsonMappingException, JsonProcessingException {
		String json=this.decryptUri(value, key);
		return this.objMapper.readValue(json, response);
	}





}
