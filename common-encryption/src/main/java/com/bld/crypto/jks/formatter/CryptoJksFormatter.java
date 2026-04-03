/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.formatter.CryptoJksFormatter.java
 */
package com.bld.crypto.jks.formatter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.annotation.CryptoJks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class CryptoJksFormatter.
 *
 * @param <T> the generic type
 */
public class CryptoJksFormatter<T> extends CryptoFormatter<T> {

	private static final Logger logger = LoggerFactory.getLogger(CryptoJksFormatter.class);

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
	@SuppressWarnings("unchecked")
	protected String decrypt(String word) {
		if (this.crypto.url())
			word = this.cryptoJksUtils.decryptUri(word, this.crypto.decrypt());
		else
			word = this.cryptoJksUtils.decryptValue(word, this.crypto.decrypt());
		try {
			Map<String, String> map = this.objMapper.readValue(word, Map.class);
			String key = map.get("key");
			if (!this.crypto.value().equals(key)) {
				String errorMessage = "The \"" + this.crypto.value() + "\" value does not match the original \"" + key + "\" value";
				logger.error(errorMessage);
				throw new CryptoException(errorMessage);
			}
			word = map.get("value");
		} catch (JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
		return word;
	}

	
	/**
	 * Wraps the plain-text value in a {@code {key, value}} JSON envelope and encrypts it,
	 * aligning with the Jackson serializer behaviour.
	 *
	 * @param word the word
	 * @return the string
	 */
	protected String encryptValue(String word) {
		try {
			Map<String, String> map = new LinkedHashMap<>();
			map.put("key", this.crypto.value());
			map.put("value", word);
			word = this.objMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
		word = this.crypto.url() ? this.cryptoJksUtils.encryptUri(word, this.crypto.encrypt()) : this.cryptoJksUtils.encryptValue(word, this.crypto.encrypt());
		return word;
	}
	
}
