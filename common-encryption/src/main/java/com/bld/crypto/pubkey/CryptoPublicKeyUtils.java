/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.CryptoPublicKeyUtils.java
 */
package com.bld.crypto.pubkey;

import java.security.PublicKey;

import com.bld.crypto.bean.CryptoKeyUtils;
import com.bld.crypto.commons.KeyUtility;
import com.bld.crypto.type.InstanceType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * The Class CryptoMapPublicKeyUtils.
 */
public abstract class CryptoPublicKeyUtils extends CryptoKeyUtils {

	private final PublicKey publicKey;

	protected CryptoPublicKeyUtils(PublicKey publicKey) {
		super();
		this.publicKey = publicKey;
	}
	
	protected CryptoPublicKeyUtils(String publicKey,InstanceType instanceType) {
		super();
		this.publicKey = KeyUtility.getPublicKey(publicKey,instanceType);
	}

	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String encryptValue(String value) {
		return super.encryptValue(value, this.publicKey, InstanceType.RSA);
	}

	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param key   the key
	 * @return the string
	 */
	@Override
	protected String encryptValue(String value, final String key) {
		return this.encryptValue(value);
	}

	/**
	 * Encrypt object.
	 *
	 * @param value the value
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObject(Object value) throws JsonProcessingException {
		return this.encryptValue(this.objMapper.writeValueAsString(value));
	}

	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String decryptValue(String value) {
		return super.decryptValue(value, this.publicKey, InstanceType.RSA);
	}

	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @param key   the key
	 * @return the string
	 */
	@Override
	protected String decryptValue(String value, final String key) {
		return this.decryptValue(value);
	}

	/**
	 * Decrypt object.
	 *
	 * @param <T>      the generic type
	 * @param value    the value
	 * @param response the response
	 * @return the t
	 * @throws JsonMappingException    the json mapping exception
	 * @throws JsonProcessingException the json processing exception
	 */
	public <T> T decryptObject(String value, Class<T> response) throws JsonMappingException, JsonProcessingException {
		String json = this.decryptValue(value);
		return this.objMapper.readValue(json, response);
	}

	/**
	 * Encrypt uri.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String encryptUri(String value) {
		return super.encryptUri(value, null);
	}
	
	/**
	 * Encrypt uri.
	 *
	 * @param value the value
	 * @param key   the key
	 * @return the string
	 */
	@Override
	protected String encryptUri(String value, final String key) {
		return this.encryptUri(value);
	}

	/**
	 * Encrypt object uri.
	 *
	 * @param value the value
	 * @return the string
	 * @throws JsonProcessingException the json processing exception
	 */
	public String encryptObjectUri(Object value) throws JsonProcessingException {
		return this.encryptUri(this.objMapper.writeValueAsString(value));
	}
	
	/**
	 * Decrypt uri.
	 *
	 * @param value the value
	 * @return the string
	 */
	public String decryptUri(String value) {		
		return super.decryptUri(value, null);
	}

	/**
	 * Decrypt uri.
	 *
	 * @param value the value
	 * @param key   the key
	 * @return the string
	 */
	@Override
	protected String decryptUri(String value, final String key) {
		return this.decryptUri(value);
	}

	/**
	 * Decrypt object uri.
	 *
	 * @param <T>      the generic type
	 * @param value    the value
	 * @param response the response
	 * @return the t
	 * @throws JsonMappingException    the json mapping exception
	 * @throws JsonProcessingException the json processing exception
	 */
	public <T> T decryptObjectUri(String value, Class<T> response) throws JsonMappingException, JsonProcessingException {
		String json = this.decryptUri(value);
		return this.objMapper.readValue(json, response);
	}

}
