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
 * Abstract Jackson {@link StdScalarSerializer} that encrypts a field value before
 * writing it to the JSON output stream.
 *
 * <p>The serializer supports scalar types ({@link Number}, {@link String}), arrays,
 * and collections.  POJOs are first serialised to JSON using the injected
 * {@link ObjectMapper} and then encrypted.
 *
 * <p>Concrete subclasses implement {@link #encryptValue(String)} to supply the actual
 * encryption algorithm (AES, RSA via JKS, or RSA public key).
 *
 * @param <T> the type of the value to encrypt
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class EncryptCertificateSerializer<T> extends StdScalarSerializer<T> {

	/** Jackson {@link ObjectMapper} used to serialise POJO values to JSON before encryption. */
	@Autowired
	protected ObjectMapper objMapper;

	/**
	 * Constructs a new serializer for the given type.  The {@link ObjectMapper} will be
	 * injected by Spring.
	 *
	 * @param t the handled value type
	 */
	protected EncryptCertificateSerializer(Class<T> t) {
		super(t);
	}

	/**
	 * Constructs a new serializer for the given type with an explicit {@link ObjectMapper}.
	 * Use this constructor when Spring injection is not available (e.g., in tests).
	 *
	 * @param t         the handled value type
	 * @param objMapper the Jackson {@link ObjectMapper} instance
	 */
	protected EncryptCertificateSerializer(Class<T> t, ObjectMapper objMapper) {
		super(t);
		this.objMapper = objMapper;
	}

	/**
	 * Serialises and encrypts the value, writing the result as a JSON string (or JSON
	 * array when the value is a {@link Collection} or array).
	 *
	 * @param value    the value to encrypt and serialise; nothing is written if {@code null}
	 * @param gen      the Jackson {@link JsonGenerator}
	 * @param provider the serialiser provider
	 * @throws IOException if a JSON I/O error occurs
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
	 * Encrypts the given plain-text string.  Concrete subclasses supply the algorithm
	 * and key selection logic.
	 *
	 * @param word the plain-text string to encrypt
	 * @return the encrypted, Base64-encoded string
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
