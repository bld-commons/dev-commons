/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.UpperLowerDeserializer.java
 */
package com.bld.crypto.pubkey.deserializer;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.deserializer.DecryptCertificateDeserializer;
import com.bld.crypto.pubkey.CryptoPubKeyData;
import com.bld.crypto.pubkey.CryptoPublicKeyUtils;
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
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class DecryptPubKeyDeserializer<T> extends DecryptCertificateDeserializer<T> implements ContextualDeserializer {

	private CryptoPubKeyData cryptoPubKey;

	@Autowired
	private CryptoPublicKeyUtils cryptoPubKeyUtils;

	/**
	 * Instantiates a new upper lower deserializer.
	 */
	public DecryptPubKeyDeserializer() {
		super(Object.class);
	}

	/**
	 * Instantiates a new upper lower deserializer.
	 *
	 * @param vc the vc
	 */
	private DecryptPubKeyDeserializer(JavaType javaType, CryptoPubKeyData cryptoPubKey, CryptoPublicKeyUtils cryptoPubKeyUtils, ObjectMapper objMapper) {
		super(javaType, objMapper);
		init(cryptoPubKey, cryptoPubKeyUtils);
	}

	private DecryptPubKeyDeserializer(JavaType javaType, Class<?> classListType, CryptoPubKeyData cryptoPubKey, CryptoPublicKeyUtils cryptoPubKeyUtils, ObjectMapper objMapper) {
		super(javaType, classListType, objMapper);
		init(cryptoPubKey, cryptoPubKeyUtils);

	}

	private void init(CryptoPubKeyData cryptoPubKey, CryptoPublicKeyUtils cryptoPubKeyUtils) {
		this.cryptoPubKey = cryptoPubKey;
		this.cryptoPubKeyUtils = cryptoPubKeyUtils;
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
		CryptoPubKeyData cryptoPubKeyData = null;
		if (property.getAnnotation(CryptoPubKey.class) != null) {
			CryptoPubKey cryptoPubKey = property.getAnnotation(CryptoPubKey.class);
			cryptoPubKeyData = new CryptoPubKeyData(cryptoPubKey.value(), cryptoPubKey.url());
		} else if (property.getAnnotation(DecryptPubKey.class) != null) {
			DecryptPubKey decryptPubKey = property.getAnnotation(DecryptPubKey.class);
			cryptoPubKeyData = new CryptoPubKeyData(decryptPubKey.value(), decryptPubKey.url());
		}

		JavaType type = ctxt.getContextualType() != null ? ctxt.getContextualType() : property.getMember().getType();

		if (property.getType() != null && property.getType().getRawClass() != null) {
			if (Collection.class.isAssignableFrom(property.getType().getRawClass()))
				return new DecryptPubKeyDeserializer<>(type, type.getContentType().getRawClass(), cryptoPubKeyData, this.cryptoPubKeyUtils, this.objMapper);
			else
				return new DecryptPubKeyDeserializer<>(property.getType(), cryptoPubKeyData, this.cryptoPubKeyUtils, this.objMapper);
		} else
			return this;
	}

	protected String decrypt(String word) {
		if (this.cryptoPubKey.isUrl())
			word = this.cryptoPubKeyUtils.decryptUri(word, this.cryptoPubKey.getName());
		else
			word = this.cryptoPubKeyUtils.decryptValue(word, this.cryptoPubKey.getName());
		return word;
	}

}
