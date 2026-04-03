/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.formatter.CryptoPkcs12Formatter.java
 */
package com.bld.crypto.pkcs12.formatter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.pkcs12.CryptoPkcs12Utils;
import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Spring {@link org.springframework.format.Formatter} that encrypts a field value on
 * {@code print} and decrypts it on {@code parse} using the X25519 + AES-256-GCM scheme,
 * enabling transparent PKCS12 encryption for Spring MVC request parameters.
 *
 * @param <T> the field type handled by this formatter
 */
public class CryptoPkcs12Formatter<T> extends CryptoFormatter<T> {

	private static final Logger logger = LoggerFactory.getLogger(CryptoPkcs12Formatter.class);

	/** The annotation instance resolved for the current field. */
	private final CryptoPkcs12 crypto;

	/** The PKCS12 encryption utility. */
	private final CryptoPkcs12Utils cryptoPkcs12Utils;


	/**
	 * Constructs a new {@code CryptoPkcs12Formatter}.
	 *
	 * @param crypto            the resolved {@link CryptoPkcs12} annotation
	 * @param cryptoPkcs12Utils the encryption utility
	 * @param objMapper         the Jackson {@link ObjectMapper}
	 * @param fieldType         the Java type of the annotated field
	 */
	public CryptoPkcs12Formatter(CryptoPkcs12 crypto, CryptoPkcs12Utils cryptoPkcs12Utils, ObjectMapper objMapper, Class<T> fieldType) {
		super(objMapper, fieldType);
		this.crypto = crypto;
		this.cryptoPkcs12Utils = cryptoPkcs12Utils;
	}


	/**
	 * Decrypts the given cipher text using the PKCS12 private key.
	 *
	 * @param word the encrypted string to decrypt
	 * @return the plain-text string
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected String decrypt(String word) {
		word = this.crypto.url()
				? this.cryptoPkcs12Utils.decryptUri(word)
				: this.cryptoPkcs12Utils.decryptValue(word);
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
	 * Wraps the plain-text value in a {@code {key, value}} JSON envelope and encrypts it
	 * using the PKCS12 public key, aligning with the Jackson serializer behaviour.
	 *
	 * @param word the plain-text string to encrypt
	 * @return the encrypted string
	 */
	@Override
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
		return this.crypto.url()
				? this.cryptoPkcs12Utils.encryptUri(word)
				: this.cryptoPkcs12Utils.encryptValue(word);
	}

}
