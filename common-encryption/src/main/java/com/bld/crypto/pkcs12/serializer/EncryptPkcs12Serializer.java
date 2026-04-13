/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.serializer.EncryptPkcs12Serializer.java
 */
package com.bld.crypto.pkcs12.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.exception.CryptoException;
import com.bld.crypto.introspector.CryptoTypeUseAnnotationIntrospector;
import com.bld.crypto.pkcs12.CryptoPkcs12Utils;
import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;
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
 * Jackson {@link ContextualSerializer} that encrypts a field value before writing it
 * to the JSON output stream using the X25519 + AES-256-GCM scheme.
 *
 * <p>The plain-text value is first wrapped in a small JSON envelope
 * {@code {"key":"&lt;annotationValue&gt;","value":"&lt;plainText&gt;"}} and then encrypted.
 * This envelope is verified during decryption to prevent cross-field substitution.
 *
 * @param <T> the type of the value to encrypt
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class EncryptPkcs12Serializer<T> extends EncryptCertificateSerializer<T> implements ContextualSerializer {

	private static final Logger logger = LoggerFactory.getLogger(EncryptPkcs12Serializer.class);

	/** The annotation instance resolved for the current field. */
	private CryptoPkcs12 crypto;

	/** The PKCS12 encryption utility bean. */
	@Autowired
	private CryptoPkcs12Utils cryptoPkcs12Utils;


	/**
	 * No-arg constructor required by Jackson when Spring injection is used.
	 */
	public EncryptPkcs12Serializer() {
		this(null, null);
	}

	/**
	 * Private constructor used when creating a contextual instance.
	 *
	 * @param t      the field type
	 * @param crypto the resolved annotation
	 */
	private EncryptPkcs12Serializer(Class<T> t, CryptoPkcs12 crypto) {
		super(t);
		this.crypto = crypto;
	}

	/**
	 * Private constructor used when creating a contextual instance with explicit dependencies.
	 *
	 * @param t                  the field type
	 * @param crypto             the resolved annotation
	 * @param cryptoPkcs12Utils  the encryption utility
	 * @param objMapper          the Jackson object mapper
	 */
	private EncryptPkcs12Serializer(Class<T> t, CryptoPkcs12 crypto, CryptoPkcs12Utils cryptoPkcs12Utils, ObjectMapper objMapper) {
		super(t, objMapper);
		this.crypto = crypto;
		this.cryptoPkcs12Utils = cryptoPkcs12Utils;
	}


	/**
	 * Resolves the {@link CryptoPkcs12} annotation from the current bean property and
	 * returns a properly typed contextual serializer.
	 *
	 * @param prov     the serializer provider
	 * @param property the bean property being serialised
	 * @return a contextual {@link EncryptPkcs12Serializer}
	 * @throws JsonMappingException if contextual resolution fails
	 */
	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		this.crypto = property.getAnnotation(CryptoPkcs12.class);
		if (this.crypto == null)
			this.crypto = CryptoTypeUseAnnotationIntrospector.findAnnotationOnTypeParam(property, CryptoPkcs12.class);
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new EncryptPkcs12Serializer<>(property.getType().getRawClass(), this.crypto, this.cryptoPkcs12Utils, this.objMapper);
		else
			return this;
	}


	/**
	 * Wraps the plain-text value in a {@code {key, value}} JSON envelope and encrypts it.
	 *
	 * @param word the plain-text string to encrypt
	 * @return the encrypted, Base64-encoded string
	 * @throws CryptoException if encryption or JSON serialisation fails
	 */
	@Override
	protected String encryptValue(String word) {
		Map<String, String> map = new HashMap<>();
		map.put("key", crypto.value());
		map.put("value", word);
		try {
			word = this.objMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
		word = this.crypto.url()
				? this.cryptoPkcs12Utils.encryptUri(word)
				: this.cryptoPkcs12Utils.encryptValue(word);
		return word;
	}

}
