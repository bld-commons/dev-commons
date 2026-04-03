/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.formatter.CryptoHmacFormatter.java
 */
package com.bld.crypto.hmac.formatter;

import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.hmac.CryptoHmacUtils;
import com.bld.crypto.hmac.annotation.CryptoHmac;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Spring {@link org.springframework.format.Formatter} that signs a field value on
 * {@code print} and verifies it on {@code parse} using HMAC, enabling transparent
 * HMAC authentication for Spring MVC request parameters and path variables.
 *
 * @param <T> the field type handled by this formatter
 */
public class CryptoHmacFormatter<T> extends CryptoFormatter<T> {

	private final CryptoHmac cryptoHmac;

	private final CryptoHmacUtils cryptoHmacUtils;

	public CryptoHmacFormatter(ObjectMapper objMapper, Class<T> fieldType, CryptoHmac cryptoHmac, CryptoHmacUtils cryptoHmacUtils) {
		super(objMapper, fieldType);
		this.cryptoHmac = cryptoHmac;
		this.cryptoHmacUtils = cryptoHmacUtils;
	}

	@Override
	protected String encryptValue(String word) {
		return this.cryptoHmac.url()
				? this.cryptoHmacUtils.signUri(this.cryptoHmac.value(), word)
				: this.cryptoHmacUtils.signValue(this.cryptoHmac.value(), word);
	}

	@Override
	protected String decrypt(String word) {
		return this.cryptoHmac.url()
				? this.cryptoHmacUtils.verifyUri(word, this.cryptoHmac.value())
				: this.cryptoHmacUtils.verifyValue(word, this.cryptoHmac.value());
	}
}
