/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.deserializer.DecryptCertificateDeserializer.java
 */
package com.bld.crypto.deserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bld.crypto.exception.CryptoException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;


/**
 * Abstract Jackson {@link StdScalarDeserializer} that decrypts a JSON string field
 * before converting it to the target Java type.
 *
 * <p>The deserializer handles scalar types ({@link Number}, {@link String}), arrays,
 * and {@link Collection} types.  After decryption, POJO values are deserialised from
 * JSON using the injected {@link ObjectMapper}.
 *
 * <p>Concrete subclasses implement {@link #decrypt(String)} to supply the actual
 * decryption algorithm (AES, RSA via JKS, or RSA public key).
 *
 * @param <T> the target Java type after decryption
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class DecryptCertificateDeserializer<T> extends StdScalarDeserializer<T>{

	/** Logger for this class. */
	private final static Logger logger = LoggerFactory.getLogger(DecryptCertificateDeserializer.class);

	/** Jackson {@link ObjectMapper} used to deserialise POJO values from JSON after decryption. */
	@Autowired
	protected ObjectMapper objMapper;

	/** The raw class of the target field type. */
	protected Class<T> fieldType;

	/** The element type when the field is a {@link Collection} or array. */
	protected Class<?> listFieldType;

	/** The name of the JSON field currently being deserialised (used for error messages). */
	protected String fieldName;

	/**
	 * Constructs a new deserializer for the given raw type.  Used for the no-arg
	 * public constructor required by Jackson when Spring injection is used.
	 *
	 * @param t the handled value type
	 */
	protected DecryptCertificateDeserializer(Class<?> t) {
		super(t);
	}

	/**
	 * Constructs a new deserializer for the given {@link JavaType} with an explicit
	 * {@link ObjectMapper}.
	 *
	 * @param javaType  the Jackson type descriptor of the target field
	 * @param objMapper the Jackson {@link ObjectMapper} instance
	 */
	protected DecryptCertificateDeserializer(JavaType javaType,ObjectMapper objMapper) {
		super(javaType);
		this.fieldType = (Class<T>) javaType.getRawClass();
		this.objMapper=objMapper;
	}

	/**
	 * Constructs a new deserializer for a {@link Collection} field.
	 *
	 * @param javaType      the Jackson type descriptor of the collection field
	 * @param listFieldType the element type of the collection
	 * @param objMapper     the Jackson {@link ObjectMapper} instance
	 */
	protected DecryptCertificateDeserializer(JavaType javaType,Class<?> listFieldType,ObjectMapper objMapper) {
		this(javaType,objMapper);
		this.listFieldType=listFieldType;
	}

	
	/**
	 * Deserialize.
	 *
	 * @param p    the p
	 * @param ctxt the ctxt
	 * @return the string
	 * @throws IOException      Signals that an I/O exception has occurred.
	 * @throws JacksonException the jackson exception
	 */
	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {

		T value = null;
		try {
			fieldName=p.currentName();
			if (this.isAssignableFrom(Collection.class)) {
				List<Object> list=(List<Object>) objMapper.readValue(p, List.class);
				for(int i=0;i<list.size();i++) {
					Object item=this.getValue(this.decrypt(list.get(i).toString()), this.listFieldType);
					list.set(i,item);
				}
					
				String parser=objMapper.writeValueAsString(list);
				Collection<Object> values=(Collection<Object>) objMapper.readValue(parser, this.fieldType);
				value=(T)values;
					
			} else if (this.isAssignableFrom(Object[].class)) {
				logger.info("is array");
				List<String> list = listCrypto(p);
				Object[] objects=new Object[list.size()];
				for(int i=0;i<list.size();i++) 
					objects[i]=this.getValue(this.decrypt(list.get(i).toString()), this.fieldType.getComponentType());
				String parser=objMapper.writeValueAsString(objects);
				Object[] values=(Object[]) objMapper.readValue(parser, this.fieldType);
				value=(T)values;
				
			} else {
				String word = p.getText();
				word = decrypt(word);
				value = getValue(word, this.fieldType);

			}
		} catch (IllegalArgumentException | SecurityException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}

		return value;
	}

	/**
	 * Gets the value.
	 *
	 * @param <F> the generic type
	 * @param word the word
	 * @param classField the class field
	 * @return the value
	 * @throws JsonMappingException the json mapping exception
	 * @throws JsonProcessingException the json processing exception
	 */
	private <F> F getValue(String word, Class<F> classField) throws JsonMappingException, JsonProcessingException {
		F value = null;
		if (StringUtils.isNoneBlank(word)) {
			if (Number.class.isAssignableFrom(classField)) {
				Double number = Double.valueOf(word);
				if (Integer.class.isAssignableFrom(classField))
					value = (F) Integer.valueOf(word);
				else if (BigDecimal.class.isAssignableFrom(classField))
					value = (F) BigDecimal.valueOf(number);
				else if (BigInteger.class.isAssignableFrom(classField))
					value = (F) new BigInteger(word);
				else if (Long.class.isAssignableFrom(classField))
					value = (F) Long.valueOf(word);
				else if (Float.class.isAssignableFrom(classField))
					value = (F) Float.valueOf(word);
				else
					value = (F) number;
			} else if(String.class.isAssignableFrom(classField))
				value = (F) word;
			else
				value=this.objMapper.readValue(word, classField);
		}
		return value;
	}

	/**
	 * Checks if is assignable from.
	 *
	 * @param classField the class field
	 * @return true, if is assignable from
	 */
	private boolean isAssignableFrom(Class<?> classField) {
		return classField.isAssignableFrom(super._valueClass);
	}

	/**
	 * Decrypts the given cipher-text string.  Concrete subclasses supply the algorithm
	 * and key selection logic.
	 *
	 * @param word the Base64-encoded cipher text to decrypt
	 * @return the plain-text string
	 */
	protected abstract String decrypt(String word);


	/**
	 * List crypto.
	 *
	 * @param p the p
	 * @return the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private List<String> listCrypto(JsonParser p) throws IOException {
		JsonToken nextValue = p.nextValue();
		List<String> list = new ArrayList<>();
		do {
			if (StringUtils.isNotBlank(p.getText()))
				list.add(p.getText());
			nextValue = p.nextValue();
		} while (nextValue != JsonToken.END_ARRAY && nextValue != JsonToken.END_OBJECT);
		return list;
	}
}
