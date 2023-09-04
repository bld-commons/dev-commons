/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.CryptoAesUtils.java
 */
package com.bld.crypto.aes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bld.crypto.aes.config.data.CipherAesSecret;
import com.bld.crypto.bean.CryptoKeyUtils;
import com.bld.crypto.type.InstanceType;

/**
 * The Class CryptoAesUtils.
 */
@Component
public class CryptoAesUtils extends CryptoKeyUtils{

	/** The cipher aes secret. */
	@Autowired
	private CipherAesSecret cipherAesSecret;
	
	/**
	 * Instance type.
	 *
	 * @return the instance type
	 */
	@Override
	protected InstanceType instanceType() {
		return InstanceType.AES;
	}

	/**
	 * Encrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String encryptValue(String value, String key) {
		return super.encryptValue(value, cipherAesSecret.getSecretKey(key));
	}

	/**
	 * Decrypt value.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String decryptValue(String value, String key) {
		return super.decryptValue(value, cipherAesSecret.getSecretKey(key));
	}

	/**
	 * Encrypt uri.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String encryptUri(String value, String key) {
		return super.encryptUri(value, key);
	}

	/**
	 * Decrypt uri.
	 *
	 * @param value the value
	 * @param key the key
	 * @return the string
	 */
	@Override
	public String decryptUri(String value, String key) {
		return super.decryptUri(value, key);
	}
	
	

}
