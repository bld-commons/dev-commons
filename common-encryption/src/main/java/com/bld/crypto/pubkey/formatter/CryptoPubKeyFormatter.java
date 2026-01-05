/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.formatter.CryptoPubKeyFormatter.java
 */
package com.bld.crypto.pubkey.formatter;

import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.pubkey.CryptoMapPublicKeyUtils;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class CryptoPubKeyFormatter.
 *
 * @param <T> the generic type
 */
public class CryptoPubKeyFormatter<T> extends CryptoFormatter<T> {

	/** The crypto pub key. */
	private CryptoPubKey cryptoPubKey;

	/** The crypto pub key utils. */
	private CryptoMapPublicKeyUtils cryptoPubKeyUtils;

	/**
	 * Instantiates a new crypto pub key formatter.
	 *
	 * @param objMapper the obj mapper
	 * @param fieldType the field type
	 * @param cryptoPubKey the crypto pub key
	 * @param cryptoPublicKeyUtils the crypto public key utils
	 */
	public CryptoPubKeyFormatter(ObjectMapper objMapper, Class<T> fieldType, CryptoPubKey cryptoPubKey, CryptoMapPublicKeyUtils cryptoPublicKeyUtils) {
		super(objMapper, fieldType);
		this.cryptoPubKey = cryptoPubKey;
		this.cryptoPubKeyUtils = cryptoPublicKeyUtils;
	}

	/**
	 * Encrypt value.
	 *
	 * @param word the word
	 * @return the string
	 */
	@Override
	protected String encryptValue(String word) {
		return this.cryptoPubKey.url() ? this.cryptoPubKeyUtils.encryptUri(word, this.cryptoPubKey.value()) : this.cryptoPubKeyUtils.encryptValue(word, this.cryptoPubKey.value());
	}

	/**
	 * Decrypt.
	 *
	 * @param word the word
	 * @return the string
	 */
	@Override
	protected String decrypt(String word) {
		if (this.cryptoPubKey.url())
			word = this.cryptoPubKeyUtils.decryptUri(word, this.cryptoPubKey.value());
		else
			word = this.cryptoPubKeyUtils.decryptValue(word, this.cryptoPubKey.value());
		return word;
	}
	
	
}
