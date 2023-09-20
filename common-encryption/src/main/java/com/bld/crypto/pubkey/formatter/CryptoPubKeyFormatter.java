package com.bld.crypto.pubkey.formatter;

import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.pubkey.CryptoPublicKeyUtils;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CryptoPubKeyFormatter<T> extends CryptoFormatter<T> {

	private CryptoPubKey cryptoPubKey;

	private CryptoPublicKeyUtils cryptoPubKeyUtils;

	public CryptoPubKeyFormatter(ObjectMapper objMapper, Class<T> fieldType, CryptoPubKey cryptoPubKey, CryptoPublicKeyUtils cryptoPublicKeyUtils) {
		super(objMapper, fieldType);
		this.cryptoPubKey = cryptoPubKey;
		this.cryptoPubKeyUtils = cryptoPublicKeyUtils;
	}

	@Override
	protected String encryptValue(String word) {
		return this.cryptoPubKey.url() ? this.cryptoPubKeyUtils.encryptUri(word, this.cryptoPubKey.value()) : this.cryptoPubKeyUtils.encryptValue(word, this.cryptoPubKey.value());
	}

	@Override
	protected String decrypt(String word) {
		if (this.cryptoPubKey.url())
			word = this.cryptoPubKeyUtils.decryptUri(word, this.cryptoPubKey.value());
		else
			word = this.cryptoPubKeyUtils.decryptValue(word, this.cryptoPubKey.value());
		return word;
	}
	
	
}
