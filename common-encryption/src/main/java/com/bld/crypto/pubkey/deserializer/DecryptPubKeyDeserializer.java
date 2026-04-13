/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.deserializer.DecryptPubKeyDeserializer.java
 */
package com.bld.crypto.pubkey.deserializer;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.bean.CryptoKeyData;
import com.bld.crypto.deserializer.DecryptCertificateDeserializer;
import com.bld.crypto.pubkey.CryptoMapPublicKeyUtils;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;
import com.bld.crypto.pubkey.annotations.DecryptPubKey;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;


/**
 * The Class UpperLowerDeserializer.
 *
 * @param <T> the generic type
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class DecryptPubKeyDeserializer<T> extends DecryptCertificateDeserializer<T> implements ContextualDeserializer {

	/** The crypto pub key. */
	private CryptoKeyData cryptoPubKey;

	/** The crypto map public key utils. */
	@Autowired
	private CryptoMapPublicKeyUtils cryptoMapPublicKeyUtils;

	/**
	 * Instantiates a new upper lower deserializer.
	 */
	public DecryptPubKeyDeserializer() {
		super(Object.class);
	}

	/**
	 * Instantiates a new upper lower deserializer.
	 *
	 * @param javaType the java type
	 * @param cryptoPubKey the crypto pub key
	 * @param cryptoPubKeyUtils the crypto pub key utils
	 * @param objMapper the obj mapper
	 */
	private DecryptPubKeyDeserializer(JavaType javaType, CryptoKeyData cryptoPubKey, CryptoMapPublicKeyUtils cryptoPubKeyUtils, ObjectMapper objMapper) {
		super(javaType, objMapper);
		init(cryptoPubKey, cryptoPubKeyUtils);
	}

	/**
	 * Instantiates a new decrypt pub key deserializer.
	 *
	 * @param javaType the java type
	 * @param classListType the class list type
	 * @param cryptoPubKey the crypto pub key
	 * @param cryptoPubKeyUtils the crypto pub key utils
	 * @param objMapper the obj mapper
	 */
	private DecryptPubKeyDeserializer(JavaType javaType, Class<?> classListType, CryptoKeyData cryptoPubKey, CryptoMapPublicKeyUtils cryptoPubKeyUtils, ObjectMapper objMapper) {
		super(javaType, classListType, objMapper);
		init(cryptoPubKey, cryptoPubKeyUtils);

	}

	/**
	 * Inits the.
	 *
	 * @param cryptoPubKey the crypto pub key
	 * @param cryptoPubKeyUtils the crypto pub key utils
	 */
	private void init(CryptoKeyData cryptoPubKey, CryptoMapPublicKeyUtils cryptoPubKeyUtils) {
		this.cryptoPubKey = cryptoPubKey;
		this.cryptoMapPublicKeyUtils = cryptoPubKeyUtils;
	}

	/**
	 * Creates the contextual.
	 *
	 * @param ctxt     the ctxt
	 * @param property the property
	 * @return the json deserializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		CryptoKeyData cryptoPubKeyData = null;
		if (property.getAnnotation(CryptoPubKey.class) != null) {
			CryptoPubKey cryptoPubKey = property.getAnnotation(CryptoPubKey.class);
			cryptoPubKeyData = new CryptoKeyData(cryptoPubKey.value(), cryptoPubKey.url());
		} else if (property.getAnnotation(DecryptPubKey.class) != null) {
			DecryptPubKey decryptPubKey = property.getAnnotation(DecryptPubKey.class);
			cryptoPubKeyData = new CryptoKeyData(decryptPubKey.value(), decryptPubKey.url());
		}

		JavaType type = ctxt.getContextualType() != null ? ctxt.getContextualType() : property.getMember().getType();

		if (property.getType() != null && property.getType().getRawClass() != null) {
			if (Collection.class.isAssignableFrom(property.getType().getRawClass()))
				return new DecryptPubKeyDeserializer<>(type, type.getContentType().getRawClass(), cryptoPubKeyData, this.cryptoMapPublicKeyUtils, this.objMapper);
			else
				return new DecryptPubKeyDeserializer<>(property.getType(), cryptoPubKeyData, this.cryptoMapPublicKeyUtils, this.objMapper);
		} else
			return this;
	}

	/**
	 * Decrypt.
	 *
	 * @param word the word
	 * @return the string
	 */
	protected String decrypt(String word) {
		if (this.cryptoPubKey.isUrl())
			word = this.cryptoMapPublicKeyUtils.decryptUri(word, this.cryptoPubKey.getName());
		else
			word = this.cryptoMapPublicKeyUtils.decryptValue(word, this.cryptoPubKey.getName());
		return word;
	}

}
