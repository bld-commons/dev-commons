/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.deserializer.DecryptAesDeserializer.java
 */
package com.bld.crypto.aes.deserializer;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.aes.CryptoAesUtils;
import com.bld.crypto.aes.annotation.CryptoAes;
import com.bld.crypto.bean.CryptoKeyData;
import com.bld.crypto.deserializer.DecryptCertificateDeserializer;
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
public class DecryptAesDeserializer<T> extends DecryptCertificateDeserializer<T> implements ContextualDeserializer {

	/** The crypto key data. */
	private CryptoKeyData cryptoKeyData;

	/** The crypto aes utils. */
	@Autowired
	private CryptoAesUtils cryptoAesUtils;

	/**
	 * Instantiates a new upper lower deserializer.
	 */
	public DecryptAesDeserializer() {
		super(Object.class);
	}

	/**
	 * Instantiates a new upper lower deserializer.
	 *
	 * @param javaType the java type
	 * @param cryptoKeyData the crypto pub key
	 * @param cryptoAesUtils the crypto pub key utils
	 * @param objMapper the obj mapper
	 */
	private DecryptAesDeserializer(JavaType javaType, CryptoKeyData cryptoKeyData, CryptoAesUtils cryptoAesUtils, ObjectMapper objMapper) {
		super(javaType, objMapper);
		init(cryptoKeyData, cryptoAesUtils);
	}

	/**
	 * Instantiates a new decrypt pub key deserializer.
	 *
	 * @param javaType the java type
	 * @param classListType the class list type
	 * @param cryptoKeyData the crypto pub key
	 * @param cryptoAesUtils the crypto pub key utils
	 * @param objMapper the obj mapper
	 */
	private DecryptAesDeserializer(JavaType javaType, Class<?> classListType, CryptoKeyData cryptoKeyData, CryptoAesUtils cryptoAesUtils, ObjectMapper objMapper) {
		super(javaType, classListType, objMapper);
		init(cryptoKeyData, cryptoAesUtils);

	}

	/**
	 * Inits the.
	 *
	 * @param cryptoKeyData the crypto pub key
	 * @param cryptoAesUtils the crypto pub key utils
	 */
	private void init(CryptoKeyData cryptoKeyData, CryptoAesUtils cryptoAesUtils) {
		this.cryptoKeyData = cryptoKeyData;
		this.cryptoAesUtils = cryptoAesUtils;
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
		CryptoAes cryptoAes = property.getAnnotation(CryptoAes.class);
		CryptoKeyData cryptoKeyData = new CryptoKeyData(cryptoAes.value(), cryptoAes.url());
		JavaType type = ctxt.getContextualType() != null ? ctxt.getContextualType() : property.getMember().getType();
		if (property.getType() != null && property.getType().getRawClass() != null) {
			if (Collection.class.isAssignableFrom(property.getType().getRawClass()))
				return new DecryptAesDeserializer<>(type, type.getContentType().getRawClass(), cryptoKeyData, this.cryptoAesUtils, this.objMapper);
			else
				return new DecryptAesDeserializer<>(property.getType(), cryptoKeyData, this.cryptoAesUtils, this.objMapper);
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
		if (this.cryptoKeyData.isUrl())
			word = this.cryptoAesUtils.decryptUri(word, this.cryptoKeyData.getName());
		else
			word = this.cryptoAesUtils.decryptValue(word, this.cryptoKeyData.getName());
		return word;
	}

}
