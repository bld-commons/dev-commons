/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.formatter.CryptoJksFormatter.java
 */
package com.bld.crypto.jks.formatter;



import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.annotation.CryptoJks;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class CryptoJksFormatter.
 *
 * @param <T> the generic type
 */
public class CryptoJksFormatter<T> extends CryptoFormatter<T> {


	/** The upper lower. */
	private CryptoJks crypto;

	/** The crypto jks utils. */
	private CryptoJksUtils cryptoJksUtils;
	
	/**
	 * Instantiates a new crypto jks formatter.
	 *
	 * @param crypto the crypto
	 * @param cryptoJksUtils the crypto jks utils
	 * @param objMapper the obj mapper
	 * @param fieldType the field type
	 */
	public CryptoJksFormatter(CryptoJks crypto, CryptoJksUtils cryptoJksUtils, ObjectMapper objMapper, Class<T> fieldType) {
		super(objMapper,fieldType);
		this.crypto = crypto;
		this.cryptoJksUtils = cryptoJksUtils;
	}



	/**
	 * Decrypt.
	 *
	 * @param word the word
	 * @return the string
	 */
	protected String decrypt(String word) {
		return this.crypto.url()
				? this.cryptoJksUtils.decryptKeyUri(this.crypto.value(), word, this.crypto.decrypt())
				: this.cryptoJksUtils.decryptKeyValue(this.crypto.value(), word, this.crypto.decrypt());
	}

	
	/**
	 * Wraps the plain-text value in a {@code {key, value}} JSON envelope and encrypts it,
	 * aligning with the Jackson serializer behaviour.
	 *
	 * @param word the word
	 * @return the string
	 */
	protected String encryptValue(String word) {
		return this.crypto.url()
				? this.cryptoJksUtils.encryptKeyUri(this.crypto.value(), word, this.crypto.encrypt())
				: this.cryptoJksUtils.encryptKeyValue(this.crypto.value(), word, this.crypto.encrypt());
	}
	
}
