package com.bld.crypto.aes.formatter;

import com.bld.crypto.aes.CryptoAesUtils;
import com.bld.crypto.aes.annotation.CryptoAes;
import com.bld.crypto.formatter.CryptoFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CryptoAesFormatter<T> extends CryptoFormatter<T> {

	private CryptoAes cryptoAes;

	private CryptoAesUtils cryptoAesUtils;



	public CryptoAesFormatter(ObjectMapper objMapper, Class<T> fieldType, CryptoAes cryptoAes, CryptoAesUtils cryptoAesUtils) {
		super(objMapper, fieldType);
		this.cryptoAes = cryptoAes;
		this.cryptoAesUtils = cryptoAesUtils;
	}

	@Override
	protected String encryptValue(String word) {
		return this.cryptoAes.url() ? this.cryptoAesUtils.encryptUri(word, this.cryptoAes.value()) : this.cryptoAesUtils.encryptValue(word, this.cryptoAes.value());
	}

	@Override
	protected String decrypt(String word) {
		if (this.cryptoAes.url())
			word = this.cryptoAesUtils.decryptUri(word, this.cryptoAes.value());
		else
			word = this.cryptoAesUtils.decryptValue(word, this.cryptoAes.value());
		return word;
	}
	
	
}
