/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.UpperLowerDeserializer.java
 */
package com.bld.crypto.jks.deserializer;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.deserializer.DecryptCertificateDeserializer;
import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.annotations.CryptoJks;
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
@SuppressWarnings({ "serial"})
@JacksonStdImpl
public class DecryptJksDeserializer<T> extends DecryptCertificateDeserializer<T> implements ContextualDeserializer {

	/** The upper lower. */
	private CryptoJks crypto;

	@Autowired
	private CryptoJksUtils cryptoJksUtils;


	/**
	 * Instantiates a new upper lower deserializer.
	 */
	public DecryptJksDeserializer() {
		super(Object.class);
	}

	/**
	 * Instantiates a new upper lower deserializer.
	 *
	 * @param vc the vc
	 */
	private DecryptJksDeserializer(JavaType javaType, CryptoJks crypto,CryptoJksUtils cryptoJksUtils,ObjectMapper objMapper) {
		super(javaType,objMapper);
		init(crypto, cryptoJksUtils);
	}

	private void init(CryptoJks crypto, CryptoJksUtils cryptoJksUtils) {
		this.crypto = crypto;
		this.cryptoJksUtils=cryptoJksUtils;
	}

	private DecryptJksDeserializer(JavaType javaType, Class<?> classListType, CryptoJks crypto,CryptoJksUtils cryptoJksUtils,ObjectMapper objMapper) {
		super(javaType,classListType,objMapper);
		init(crypto, cryptoJksUtils);
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
		this.crypto = property.getAnnotation(CryptoJks.class);
		JavaType type = ctxt.getContextualType() != null ? ctxt.getContextualType() : property.getMember().getType();
		if (property.getType() != null && property.getType().getRawClass() != null) {
			if (Collection.class.isAssignableFrom(property.getType().getRawClass()))
				return new DecryptJksDeserializer<>(type, type.getContentType().getRawClass(), crypto,this.cryptoJksUtils,this.objMapper);
			else
				return new DecryptJksDeserializer<>(property.getType(), crypto,this.cryptoJksUtils,this.objMapper);
		} else
			return this;
	}




	protected String decrypt(String word) {
		if (this.crypto.url())
			word = this.cryptoJksUtils.decryptUri(word,this.crypto.decrypt());
		else
			word = this.cryptoJksUtils.decryptValue(word,this.crypto.decrypt());
		return word;
	}



}
