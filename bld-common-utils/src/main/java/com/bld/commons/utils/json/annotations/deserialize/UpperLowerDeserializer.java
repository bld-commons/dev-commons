/**
 * @author Francesco Baldi
 * @mail francesco.baldi1987@gmail.com
 * @class bld.commons.reflection.annotations.deserialize.UpperLowerDeserializer.java
 */
package com.bld.commons.utils.json.annotations.deserialize;

import java.io.IOException;

import com.bld.commons.utils.json.annotations.UpperLowerCase;
import com.bld.commons.utils.types.UpperLowerType;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

// TODO: Auto-generated Javadoc
/**
 * The Class UpperLowerDeserializer.
 */
@SuppressWarnings("serial")
@JacksonStdImpl
public class UpperLowerDeserializer extends StdScalarDeserializer<String> implements ContextualDeserializer {

	/** The upper lower. */
	private UpperLowerType upperLower;

	/**
	 * Instantiates a new upper lower deserializer.
	 */
	public UpperLowerDeserializer() {
		super(String.class);
	}

	/**
	 * Instantiates a new upper lower deserializer.
	 *
	 * @param vc the vc
	 */
	protected UpperLowerDeserializer(Class<?> vc) {
		super(vc);
	}

	/**
	 * Creates the contextual.
	 *
	 * @param ctxt the ctxt
	 * @param property the property
	 * @return the json deserializer
	 * @throws JsonMappingException the json mapping exception
	 */
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
		UpperLowerCase jsonUpperLower = property.getAnnotation(UpperLowerCase.class);
		this.upperLower = jsonUpperLower.value();
		return this;
	}

	/**
	 * Deserialize.
	 *
	 * @param p the p
	 * @param ctxt the ctxt
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JacksonException the jackson exception
	 */
	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		String word = p.getText();
		switch (this.upperLower) {
		case LOWER:
			word = word.toLowerCase();
			break;
		case UPPER:
			word = word.toUpperCase();
			break;
		case NONE:
		default:
			break;

		}
		return word;
	}

}
