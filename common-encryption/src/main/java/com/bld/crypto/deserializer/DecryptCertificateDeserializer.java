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
 * The Class DecryptCertificateDeserializer.
 *
 * @param <T> the generic type
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class DecryptCertificateDeserializer<T> extends StdScalarDeserializer<T>{

	/** The Constant logger. */
	private final static Logger logger = LoggerFactory.getLogger(DecryptCertificateDeserializer.class);
	
	/** The obj mapper. */
	@Autowired
	protected ObjectMapper objMapper;
	
	/** The class field. */
	protected Class<T> fieldType;

	/** The class list type. */
	protected Class<?> listFieldType;
	
	/**
	 * Instantiates a new decrypt certificate deserializer.
	 *
	 * @param t the t
	 */
	protected DecryptCertificateDeserializer(Class<?> t) {
		super(t);
	}
	
	/**
	 * Instantiates a new decrypt certificate deserializer.
	 *
	 * @param javaType the java type
	 * @param objMapper the obj mapper
	 */
	protected DecryptCertificateDeserializer(JavaType javaType,ObjectMapper objMapper) {
		super(javaType);
		this.fieldType = (Class<T>) javaType.getRawClass();
		this.objMapper=objMapper;
	}
	
	/**
	 * Instantiates a new decrypt certificate deserializer.
	 *
	 * @param javaType the java type
	 * @param listFieldType the class list type
	 * @param objMapper the obj mapper
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
	 * Decrypt.
	 *
	 * @param word the word
	 * @return the string
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
