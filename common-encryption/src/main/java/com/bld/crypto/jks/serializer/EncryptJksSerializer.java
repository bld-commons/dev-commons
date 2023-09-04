package com.bld.crypto.jks.serializer;

import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.jks.CryptoJksUtils;
import com.bld.crypto.jks.annotations.CryptoJks;
import com.bld.crypto.serializer.EncryptCertificateSerializer;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

@SuppressWarnings("serial")
@JacksonStdImpl
public class EncryptJksSerializer<T> extends EncryptCertificateSerializer<T> implements ContextualSerializer {

	/** The upper lower. */
	private CryptoJks crypto;

	@Autowired
	private CryptoJksUtils cryptoJksUtils;
	


	public EncryptJksSerializer() {
		this(null, null);
	}

	private EncryptJksSerializer(Class<T> t, CryptoJks crypto) {
		super(t);
		this.crypto = crypto;
	}
	
	private EncryptJksSerializer(Class<T> t, CryptoJks crypto,CryptoJksUtils cryptoJksUtils,ObjectMapper objMapper) {
		super(t,objMapper);
		this.crypto = crypto;
		this.cryptoJksUtils=cryptoJksUtils;
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
		this.crypto = property.getAnnotation(CryptoJks.class);
		if (property.getType() != null && property.getType().getRawClass() != null)
			return new EncryptJksSerializer<>(property.getType().getRawClass(), this.crypto,this.cryptoJksUtils,this.objMapper);
		else
			return this;
	}

	@Override
	protected String encryptValue(String word) {
		word = this.crypto.url() ? this.cryptoJksUtils.encryptUri(word, this.crypto.encrypt()) : this.cryptoJksUtils.encryptValue(word, this.crypto.encrypt());
		return word;
	}
	
}
