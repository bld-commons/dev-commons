package com.bld.crypto.jks.formatter;

import com.bld.crypto.formatter.CryptoFormatter;
import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.annotation.CryptoJks;
import com.fasterxml.jackson.databind.ObjectMapper;
public class CryptoJksFormatter<T> extends CryptoFormatter<T> {
	
	
	
	/** The upper lower. */
	private CryptoJks crypto;

	/** The crypto jks utils. */
	private CryptoJksUtils cryptoJksUtils;
	
	public CryptoJksFormatter(CryptoJks crypto, CryptoJksUtils cryptoJksUtils, ObjectMapper objMapper, Class<T> fieldType) {
		super(objMapper,fieldType);
		this.crypto = crypto;
		this.cryptoJksUtils = cryptoJksUtils;
	}



	protected String decrypt(String word) {
		if (this.crypto.url())
			word = this.cryptoJksUtils.decryptUri(word,this.crypto.decrypt());
		else
			word = this.cryptoJksUtils.decryptValue(word,this.crypto.decrypt());
		return word;
	}

	
	protected String encryptValue(String word) {
		word = this.crypto.url() ? this.cryptoJksUtils.encryptUri(word, this.crypto.encrypt()) : this.cryptoJksUtils.encryptValue(word, this.crypto.encrypt());
		return word;
	}
	
}
