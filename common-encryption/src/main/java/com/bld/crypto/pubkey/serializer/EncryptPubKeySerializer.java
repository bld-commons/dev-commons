/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.serializer.EncryptPubKeySerializer.java
 */
package com.bld.crypto.pubkey.serializer;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.bean.CryptoKeyData;
import com.bld.crypto.pubkey.CryptoPublicKeyUtils;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;
import com.bld.crypto.pubkey.annotations.EncryptPubKey;
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
public class EncryptPubKeySerializer<T> extends EncryptCertificateSerializer<T> implements ContextualSerializer {

	/** The crypto pub key. */
	private CryptoKeyData cryptoPubKey;

	/** The crypto public key utils. */
	@Autowired
	private CryptoPublicKeyUtils cryptoPublicKeyUtils;

	/**
	 * Instantiates a new encrypt pub key serializer.
	 */
	public EncryptPubKeySerializer() {
		this(null,null);
	}
	
	/**
	 * Instantiates a new encrypt pub key serializer.
	 *
	 * @param t the t
	 * @param cryptoPubKey the crypto pub key
	 */
	private EncryptPubKeySerializer(Class<T> t, CryptoKeyData cryptoPubKey) {
		super(t);
		this.cryptoPubKey = cryptoPubKey;
	}

	/**
	 * Instantiates a new encrypt pub key serializer.
	 *
	 * @param t the t
	 * @param cryptoPubKey the crypto pub key
	 * @param cryptoPublicKeyUtils the crypto public key utils
	 * @param objMapper the obj mapper
	 */
	private EncryptPubKeySerializer(Class<T> t, CryptoKeyData cryptoPubKey,CryptoPublicKeyUtils cryptoPublicKeyUtils,ObjectMapper objMapper) {
		super(t,objMapper);
		this.cryptoPubKey = cryptoPubKey;
		this.cryptoPublicKeyUtils=cryptoPublicKeyUtils;
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
		CryptoKeyData cryptoPubKeyData=null;
		if(property.getAnnotation(CryptoPubKey.class)!=null) {
			CryptoPubKey cryptoPubKey=property.getAnnotation(CryptoPubKey.class);
			cryptoPubKeyData=new CryptoKeyData(cryptoPubKey.value(), cryptoPubKey.url());
		}else if(property.getAnnotation(EncryptPubKey.class)!=null) {
			EncryptPubKey encryptPubKey=property.getAnnotation(EncryptPubKey.class);
			cryptoPubKeyData=new CryptoKeyData(encryptPubKey.value(), encryptPubKey.url());
		}
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new EncryptPubKeySerializer<>(property.getType().getRawClass(), cryptoPubKeyData,this.cryptoPublicKeyUtils,this.objMapper);
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
		return this.cryptoPubKey.isUrl() ? this.cryptoPublicKeyUtils.encryptUri(word, this.cryptoPubKey.getName()) : this.cryptoPublicKeyUtils.encryptValue(word, this.cryptoPubKey.getName());
	}

}
