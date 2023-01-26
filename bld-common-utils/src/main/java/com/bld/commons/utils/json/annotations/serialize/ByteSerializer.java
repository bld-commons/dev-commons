/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.serialize.CustomByteSerializer.java
 */
package com.bld.commons.utils.json.annotations.serialize;

import java.io.IOException;
import java.util.Base64;

import com.bld.commons.utils.json.annotations.JsonFile;
import com.bld.commons.utils.types.MimeType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * The Class ByteSerializer.
 */
@SuppressWarnings("serial")
public class ByteSerializer extends StdScalarSerializer<byte[]> implements ContextualSerializer {

	/** The mime type. */
	private MimeType mimeType;

	/**
	 * Instantiates a new custom byte serializer.
	 */
	public ByteSerializer() {
		this(null);
	}

	/**
	 * Instantiates a new custom byte serializer.
	 *
	 * @param t the t
	 */
	public ByteSerializer(Class<byte[]> t) {
		super(t);
	}

	
	
	/**
	 * File base 64.
	 *
	 * @param value the value
	 * @return the string
	 */
	private String fileBase64(byte[] value) {
		String file=Base64.getEncoder().encodeToString(value);
		if(!MimeType.none.equals(this.mimeType))
			file="data:" + this.mimeType.getMimeType() + ";base64,"+file;
		return file;
	}
	
	
	/**
	 * Serialize.
	 *
	 * @param value    the value
	 * @param gen      the gen
	 * @param provider the provider
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void serialize(byte[] value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(fileBase64(value));
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
		JsonFile jsonFile = property.getAnnotation(JsonFile.class);
		this.mimeType = jsonFile.mimeType();
		return this;
	}

}
