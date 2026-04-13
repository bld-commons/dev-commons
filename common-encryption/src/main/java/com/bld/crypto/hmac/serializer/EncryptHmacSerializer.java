/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.serializer.EncryptHmacSerializer.java
 */
package com.bld.crypto.hmac.serializer;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.bean.CryptoKeyData;
import com.bld.crypto.hmac.CryptoHmacUtils;
import com.bld.crypto.hmac.annotation.CryptoHmac;
import com.bld.crypto.introspector.CryptoTypeUseAnnotationIntrospector;
import com.bld.crypto.serializer.EncryptCertificateSerializer;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * Jackson {@link ContextualSerializer} that signs a field value before writing it
 * to the JSON output stream using HMAC.
 *
 * <p>The plain-text value is wrapped in a JSON envelope
 * {@code {"key":"&lt;annotationValue&gt;","value":"&lt;plainText&gt;"}} and then signed.
 * The envelope is verified during deserialisation to prevent cross-field substitution.
 *
 * @param <T> the type of the value to sign
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class EncryptHmacSerializer<T> extends EncryptCertificateSerializer<T> implements ContextualSerializer {

	/** Holds the field identifier and URL flag resolved from the annotation. */
	private CryptoKeyData cryptoKeyData;

	@Autowired
	private CryptoHmacUtils cryptoHmacUtils;

	public EncryptHmacSerializer() {
		this(null, null);
	}

	private EncryptHmacSerializer(Class<T> t, CryptoKeyData cryptoKeyData) {
		super(t);
		this.cryptoKeyData = cryptoKeyData;
	}

	private EncryptHmacSerializer(Class<T> t, CryptoKeyData cryptoKeyData, CryptoHmacUtils cryptoHmacUtils, ObjectMapper objMapper) {
		super(t, objMapper);
		this.cryptoKeyData = cryptoKeyData;
		this.cryptoHmacUtils = cryptoHmacUtils;
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		CryptoHmac annotation = property.getAnnotation(CryptoHmac.class);
		if (annotation == null)
			annotation = CryptoTypeUseAnnotationIntrospector.findAnnotationOnTypeParam(property, CryptoHmac.class);
		CryptoKeyData cryptoKeyData = new CryptoKeyData(annotation.value(), annotation.url());
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new EncryptHmacSerializer<>(property.getType().getRawClass(), cryptoKeyData, this.cryptoHmacUtils, this.objMapper);
		else
			return this;
	}

	@Override
	protected String encryptValue(String word) {
		return this.cryptoKeyData.isUrl()
				? this.cryptoHmacUtils.signUri(this.cryptoKeyData.getName(), word)
				: this.cryptoHmacUtils.signValue(this.cryptoKeyData.getName(), word);
	}
}
