/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.config.data.CryptoHmacSecret.java
 */
package com.bld.crypto.hmac.config.data;

import javax.crypto.SecretKey;

/**
 * Holder for the HMAC {@link SecretKey} and algorithm name produced by
 * {@link com.bld.crypto.hmac.config.HmacConfiguration}.
 */
public class CryptoHmacSecret {

	private final SecretKey secretKey;

	private final String algorithm;

	public CryptoHmacSecret(SecretKey secretKey, String algorithm) {
		this.secretKey = secretKey;
		this.algorithm = algorithm;
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public String getAlgorithm() {
		return algorithm;
	}
}
