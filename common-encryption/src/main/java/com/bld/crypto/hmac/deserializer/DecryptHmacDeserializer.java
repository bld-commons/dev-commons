/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.deserializer.DecryptHmacDeserializer.java
 */
package com.bld.crypto.hmac.deserializer;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.bean.CryptoKeyData;
import com.bld.crypto.deserializer.DecryptCertificateDeserializer;
import com.bld.crypto.hmac.CryptoHmacUtils;
import com.bld.crypto.hmac.annotation.CryptoHmac;
import com.bld.crypto.introspector.CryptoTypeUseAnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

/**
 * Jackson {@link ContextualDeserializer} that verifies an HMAC token and converts
 * the extracted plain-text value to the target Java type.
 *
 * <p>The token is expected to have been produced by {@link com.bld.crypto.hmac.serializer.EncryptHmacSerializer}.
 * Verification failure or field key mismatch throws a
 * {@link com.bld.crypto.exception.CryptoException}.
 *
 * @param <T> the target Java type after verification
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class DecryptHmacDeserializer<T> extends DecryptCertificateDeserializer<T> implements ContextualDeserializer {

	/** Holds the field identifier and URL flag resolved from the annotation. */
	private CryptoKeyData cryptoKeyData;

	@Autowired
	private CryptoHmacUtils cryptoHmacUtils;

	public DecryptHmacDeserializer() {
		super(Object.class);
	}

	private DecryptHmacDeserializer(JavaType javaType, CryptoKeyData cryptoKeyData, CryptoHmacUtils cryptoHmacUtils, ObjectMapper objMapper) {
		super(javaType, objMapper);
		init(cryptoKeyData, cryptoHmacUtils);
	}

	private DecryptHmacDeserializer(JavaType javaType, Class<?> classListType, CryptoKeyData cryptoKeyData, CryptoHmacUtils cryptoHmacUtils, ObjectMapper objMapper) {
		super(javaType, classListType, objMapper);
		init(cryptoKeyData, cryptoHmacUtils);
	}

	private void init(CryptoKeyData cryptoKeyData, CryptoHmacUtils cryptoHmacUtils) {
		this.cryptoKeyData = cryptoKeyData;
		this.cryptoHmacUtils = cryptoHmacUtils;
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		CryptoHmac annotation = property.getAnnotation(CryptoHmac.class);
		if (annotation == null)
			annotation = CryptoTypeUseAnnotationIntrospector.findAnnotationOnTypeParam(property, CryptoHmac.class);
		CryptoKeyData cryptoKeyData = new CryptoKeyData(annotation.value(), annotation.url());
		JavaType type = ctxt.getContextualType() != null ? ctxt.getContextualType() : property.getMember().getType();
		if (property.getType() != null && property.getType().getRawClass() != null) {
			if (Collection.class.isAssignableFrom(property.getType().getRawClass()))
				return new DecryptHmacDeserializer<>(type, type.getContentType().getRawClass(), cryptoKeyData, this.cryptoHmacUtils, this.objMapper);
			else
				return new DecryptHmacDeserializer<>(property.getType(), cryptoKeyData, this.cryptoHmacUtils, this.objMapper);
		} else
			return this;
	}

	@Override
	protected String decrypt(String word) {
		return this.cryptoKeyData.isUrl()
				? this.cryptoHmacUtils.verifyUri(word, this.cryptoKeyData.getName())
				: this.cryptoHmacUtils.verifyValue(word, this.cryptoKeyData.getName());
	}
}
