/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.serializer.EncryptCertificateSerializer.java
 */
package com.bld.crypto.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;


/**
 * The Class EncryptCertificateSerializer.
 *
 * @param <T> the generic type
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class EncryptCertificateSerializer<T> extends StdScalarSerializer<T> {

	/** The obj mapper. */
	@Autowired
	protected ObjectMapper objMapper;

	/**
	 * Instantiates a new encrypt certificate serializer.
	 *
	 * @param t the t
	 */
	protected EncryptCertificateSerializer(Class<T> t) {
		super(t);
	}

	/**
	 * Instantiates a new encrypt certificate serializer.
	 *
	 * @param t the t
	 * @param objMapper the obj mapper
	 */
	protected EncryptCertificateSerializer(Class<T> t, ObjectMapper objMapper) {
		super(t);
		this.objMapper = objMapper;
	}

	/**
	 * Serialize.
	 *
	 * @param value the value
	 * @param gen the gen
	 * @param provider the provider
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		String word = "";
		if (value != null) {
			if (value instanceof Collection) {
				this.writeArray(new ArrayList<>((Collection<T>) value), gen);
			} else if (value instanceof Object[]) {
				this.writeArray(Arrays.asList((T[]) value), gen);
			} else if (value instanceof Number || value instanceof String) {
				word = encrypt(value);
				gen.writeString(word);
			} else {
				word = this.encryptValue(this.objMapper.writeValueAsString(value));
				gen.writeString(word);
			}
		}

	}

	/**
	 * Encrypt.
	 *
	 * @param value the value
	 * @return the string
	 */
	private String encrypt(T value) {
		String word = value.toString();
		return encryptValue(word);
	}

	/**
	 * Encrypt value.
	 *
	 * @param word the word
	 * @return the string
	 */
	protected abstract String encryptValue(String word);

	/**
	 * Write array.
	 *
	 * @param list the list
	 * @param gen the gen
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeArray(List<T> list, JsonGenerator gen) throws IOException {
		String[] array = new String[list.size()];
		int i = 0;
		for (T value : list)
			array[i++] = this.encrypt(value);
		gen.writeArray(array, 0, i);
	}

}
