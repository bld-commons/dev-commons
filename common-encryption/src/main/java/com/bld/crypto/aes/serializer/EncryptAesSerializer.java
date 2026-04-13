/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.aes.serializer.EncryptAesSerializer.java
 */
package com.bld.crypto.aes.serializer;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.aes.CryptoAesUtils;
import com.bld.crypto.aes.annotation.CryptoAes;
import com.bld.crypto.bean.CryptoKeyData;
import com.bld.crypto.serializer.EncryptCertificateSerializer;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;


/**
 * The Class EncryptPubKeySerializer.
 *
 * @param <T> the generic type
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class EncryptAesSerializer<T> extends EncryptCertificateSerializer<T> implements ContextualSerializer {

	/** The crypto pub key. */
	private CryptoKeyData cryptoKeyData;

	/** The crypto public key utils. */
	@Autowired
	private CryptoAesUtils cryptoAesUtils;

	/**
	 * Instantiates a new encrypt pub key serializer.
	 */
	public EncryptAesSerializer() {
		this(null,null);
	}
	
	/**
	 * Instantiates a new encrypt pub key serializer.
	 *
	 * @param t the t
	 * @param cryptoKeyData the crypto pub key
	 */
	private EncryptAesSerializer(Class<T> t, CryptoKeyData cryptoKeyData) {
		super(t);
		this.cryptoKeyData = cryptoKeyData;
	}

	/**
	 * Instantiates a new encrypt pub key serializer.
	 *
	 * @param t the t
	 * @param cryptoKeyData the crypto pub key
	 * @param cryptoAesUtils the crypto public key utils
	 * @param objMapper the obj mapper
	 */
	private EncryptAesSerializer(Class<T> t, CryptoKeyData cryptoKeyData,CryptoAesUtils cryptoAesUtils,ObjectMapper objMapper) {
		super(t,objMapper);
		this.cryptoKeyData = cryptoKeyData;
		this.cryptoAesUtils=cryptoAesUtils;
	}

	/**
	 * Creates the contextual.
	 *
	 * @param prov the prov
	 * @param property the property
	 * @return the json serializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		CryptoAes cryptoAes=property.getAnnotation(CryptoAes.class);
		CryptoKeyData cryptoKeyData=new CryptoKeyData(cryptoAes.value(), cryptoAes.url());
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new EncryptAesSerializer<>(property.getType().getRawClass(), cryptoKeyData,this.cryptoAesUtils,this.objMapper);
		else
			return this;
	}


	
	/**
	 * Encrypt value.
	 *
	 * @param word the word
	 * @return the string
	 */
	@Override
	protected String encryptValue(String word) {
		return this.cryptoKeyData.isUrl() ? this.cryptoAesUtils.encryptUri(word, this.cryptoKeyData.getName()) : this.cryptoAesUtils.encryptValue(word, this.cryptoKeyData.getName());
	}

}
