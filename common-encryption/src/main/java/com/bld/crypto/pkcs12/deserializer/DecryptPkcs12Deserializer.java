/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.deserializer.DecryptPkcs12Deserializer.java
 */
package com.bld.crypto.pkcs12.deserializer;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.deserializer.DecryptCertificateDeserializer;
import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.introspector.CryptoTypeUseAnnotationIntrospector;
import com.bld.crypto.pkcs12.CryptoPkcs12Utils;
import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;


/**
 * Jackson {@link ContextualDeserializer} that decrypts a JSON string field before
 * converting it to the target Java type, using the X25519 + AES-256-GCM scheme.
 *
 * <p>After decryption the payload is expected to be a JSON envelope
 * {@code {"key":"&lt;annotationValue&gt;","value":"&lt;plainText&gt;"}}. The key is
 * validated against the annotation's {@link CryptoPkcs12#value()} to detect accidental
 * cross-field substitution and a {@link CryptoException} is thrown if they do not match.
 *
 * @param <T> the target Java type after decryption
 */
@SuppressWarnings({"serial"})
@JacksonStdImpl
public class DecryptPkcs12Deserializer<T> extends DecryptCertificateDeserializer<T> implements ContextualDeserializer {

	private static final Logger logger = LoggerFactory.getLogger(DecryptPkcs12Deserializer.class);

	/** The annotation instance resolved for the current field. */
	private CryptoPkcs12 crypto;

	/** The PKCS12 decryption utility bean. */
	@Autowired
	private CryptoPkcs12Utils cryptoPkcs12Utils;


	/**
	 * No-arg constructor required by Jackson when Spring injection is used.
	 */
	public DecryptPkcs12Deserializer() {
		super(Object.class);
	}

	/**
	 * Private constructor for scalar / POJO fields.
	 *
	 * @param javaType          the Jackson type of the target field
	 * @param crypto            the resolved annotation
	 * @param cryptoPkcs12Utils the decryption utility
	 * @param objMapper         the Jackson object mapper
	 */
	private DecryptPkcs12Deserializer(JavaType javaType, CryptoPkcs12 crypto, CryptoPkcs12Utils cryptoPkcs12Utils, ObjectMapper objMapper) {
		super(javaType, objMapper);
		init(crypto, cryptoPkcs12Utils);
	}

	/**
	 * Private constructor for {@link Collection} fields.
	 *
	 * @param javaType          the Jackson type of the target collection field
	 * @param classListType     the element type of the collection
	 * @param crypto            the resolved annotation
	 * @param cryptoPkcs12Utils the decryption utility
	 * @param objMapper         the Jackson object mapper
	 */
	private DecryptPkcs12Deserializer(JavaType javaType, Class<?> classListType, CryptoPkcs12 crypto, CryptoPkcs12Utils cryptoPkcs12Utils, ObjectMapper objMapper) {
		super(javaType, classListType, objMapper);
		init(crypto, cryptoPkcs12Utils);
	}

	/**
	 * Shared initialisation called by both private constructors.
	 */
	private void init(CryptoPkcs12 crypto, CryptoPkcs12Utils cryptoPkcs12Utils) {
		this.crypto = crypto;
		this.cryptoPkcs12Utils = cryptoPkcs12Utils;
	}


	/**
	 * Resolves the {@link CryptoPkcs12} annotation and returns a contextual deserializer
	 * typed to the target field.
	 *
	 * @param ctxt     the deserialization context
	 * @param property the bean property being deserialised
	 * @return a contextual {@link DecryptPkcs12Deserializer}
	 * @throws JsonMappingException if contextual resolution fails
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		this.crypto = property.getAnnotation(CryptoPkcs12.class);
		if (this.crypto == null)
			this.crypto = CryptoTypeUseAnnotationIntrospector.findAnnotationOnTypeParam(property, CryptoPkcs12.class);
		JavaType type = ctxt.getContextualType() != null ? ctxt.getContextualType() : property.getMember().getType();
		if (property.getType() != null && property.getType().getRawClass() != null) {
			if (Collection.class.isAssignableFrom(property.getType().getRawClass()))
				return new DecryptPkcs12Deserializer<>(type, type.getContentType().getRawClass(), crypto, this.cryptoPkcs12Utils, this.objMapper);
			else
				return new DecryptPkcs12Deserializer<>(property.getType(), crypto, this.cryptoPkcs12Utils, this.objMapper);
		} else
			return this;
	}


	/**
	 * Decrypts the cipher text, validates the key envelope, and returns the plain-text value.
	 *
	 * @param word the encrypted string from the JSON input
	 * @return the plain-text value extracted from the decrypted envelope
	 * @throws CryptoException if decryption fails or the key identifier does not match
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected String decrypt(String word) {
		word = this.crypto.url()
				? this.cryptoPkcs12Utils.decryptUri(word)
				: this.cryptoPkcs12Utils.decryptValue(word);
		try {
			Map<String, String> map = this.objMapper.readValue(word, Map.class);
			String key = map.get("key");
			if (!this.crypto.value().equals(key)) {
				String errorMessage = "In the \"" + this.fieldName + "\" field, the \""
						+ this.crypto.value() + "\" value does not match the original \""
						+ key + "\" value";
				logger.error(errorMessage);
				throw new CryptoException(errorMessage);
			}
			word = map.get("value");
		} catch (JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
		return word;
	}

}
