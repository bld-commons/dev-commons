/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.formatter.CryptoPkcs12Formatter.java
 */
package com.bld.crypto.pkcs12.formatter;

import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.pkcs12.CryptoPkcs12Utils;
import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Spring {@link org.springframework.format.Formatter} that encrypts a field value on
 * {@code print} and decrypts it on {@code parse} using the X25519 + AES-256-GCM scheme,
 * enabling transparent PKCS12 encryption for Spring MVC request parameters.
 *
 * @param <T> the field type handled by this formatter
 */
public class CryptoPkcs12Formatter<T> extends CryptoFormatter<T> {

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
	@Override
	protected String decrypt(String word) {
		return this.crypto.url()
				? this.cryptoPkcs12Utils.decryptUri(word)
				: this.cryptoPkcs12Utils.decryptValue(word);
	}


	/**
	 * Encrypts the given plain-text string using the PKCS12 public key.
	 *
	 * @param word the plain-text string to encrypt
	 * @return the encrypted string
	 */
	@Override
	protected String encryptValue(String word) {
		return this.crypto.url()
				? this.cryptoPkcs12Utils.encryptUri(word)
				: this.cryptoPkcs12Utils.encryptValue(word);
	}

}
