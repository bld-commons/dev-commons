/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.jks.serializer.EncryptJksSerializer.java
 */
package com.bld.crypto.jks.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.annotation.CryptoJks;
import com.bld.crypto.serializer.EncryptCertificateSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;



/**
 * The Class EncryptJksSerializer.
 *
 * @param <T> the generic type
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class EncryptJksSerializer<T> extends EncryptCertificateSerializer<T> implements ContextualSerializer {

	/** The upper lower. */
	private CryptoJks crypto;

	/** The crypto jks utils. */
	@Autowired
	private CryptoJksUtils cryptoJksUtils;
	
	private static final Logger logger=LoggerFactory.getLogger(EncryptJksSerializer.class);


	/**
	 * Instantiates a new encrypt jks serializer.
	 */
	public EncryptJksSerializer() {
		this(null, null);
	}

	/**
	 * Instantiates a new encrypt jks serializer.
	 *
	 * @param t the t
	 * @param crypto the crypto
	 */
	private EncryptJksSerializer(Class<T> t, CryptoJks crypto) {
		super(t);
		this.crypto = crypto;
	}
	
	/**
	 * Instantiates a new encrypt jks serializer.
	 *
	 * @param t the t
	 * @param crypto the crypto
	 * @param cryptoJksUtils the crypto jks utils
	 * @param objMapper the obj mapper
	 */
	private EncryptJksSerializer(Class<T> t, CryptoJks crypto,CryptoJksUtils cryptoJksUtils,ObjectMapper objMapper) {
		super(t,objMapper);
		this.crypto = crypto;
		this.cryptoJksUtils=cryptoJksUtils;
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
		this.crypto = property.getAnnotation(CryptoJks.class);
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new EncryptJksSerializer<>(property.getType().getRawClass(), this.crypto,this.cryptoJksUtils,this.objMapper);
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
		Map<String,String> map= new HashMap<>();
		map.put("key", crypto.value());
		map.put("value", word);
		try {
			word=this.objMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
		word = this.crypto.url() ? this.cryptoJksUtils.encryptUri(word, this.crypto.encrypt()) : this.cryptoJksUtils.encryptValue(word, this.crypto.encrypt());
		return word;
	}
	
}
