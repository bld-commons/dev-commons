/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.formatter.CryptoAesFormatter.java
 */
package com.bld.crypto.aes.formatter;

import com.bld.crypto.aes.CryptoAesUtils;
import com.bld.crypto.aes.annotation.CryptoAes;
import com.bld.crypto.formatter.CryptoFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class CryptoAesFormatter.
 *
 * @param <T> the generic type
 */
public class CryptoAesFormatter<T> extends CryptoFormatter<T> {

	/** The crypto aes. */
	private CryptoAes cryptoAes;

	/** The crypto aes utils. */
	private CryptoAesUtils cryptoAesUtils;



	/**
	 * Instantiates a new crypto aes formatter.
	 *
	 * @param objMapper the obj mapper
	 * @param fieldType the field type
	 * @param cryptoAes the crypto aes
	 * @param cryptoAesUtils the crypto aes utils
	 */
	public CryptoAesFormatter(ObjectMapper objMapper, Class<T> fieldType, CryptoAes cryptoAes, CryptoAesUtils cryptoAesUtils) {
		super(objMapper, fieldType);
		this.cryptoAes = cryptoAes;
		this.cryptoAesUtils = cryptoAesUtils;
	}

	/**
	 * Encrypt value.
	 *
	 * @param word the word
	 * @return the string
	 */
	@Override
	protected String encryptValue(String word) {
		return this.cryptoAes.url() ? this.cryptoAesUtils.encryptUri(word, this.cryptoAes.value()) : this.cryptoAesUtils.encryptValue(word, this.cryptoAes.value());
	}

	/**
	 * Decrypt.
	 *
	 * @param word the word
	 * @return the string
	 */
	@Override
	protected String decrypt(String word) {
		if (this.cryptoAes.url())
			word = this.cryptoAesUtils.decryptUri(word, this.cryptoAes.value());
		else
			word = this.cryptoAesUtils.decryptValue(word, this.cryptoAes.value());
		return word;
	}
	
	
}
