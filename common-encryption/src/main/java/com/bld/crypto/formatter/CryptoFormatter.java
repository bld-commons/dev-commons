package com.bld.crypto.formatter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.Formatter;

import com.bld.crypto.exception.CryptoException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("unchecked")
public abstract class CryptoFormatter<T> implements Formatter<T> {

	private ObjectMapper objMapper;
	private Class<T> fieldType;

	private final static Logger logger = LoggerFactory.getLogger(CryptoFormatter.class);

	public CryptoFormatter(ObjectMapper objMapper, Class<T> fieldType) {
		super();
		this.objMapper = objMapper;
		this.fieldType = fieldType;
	}

	@Override
	public String print(T value, Locale locale) {
		try {
			if (value != null) 
				if (value instanceof Number || value instanceof String)
					return encrypt(value);
				else
					return this.encryptValue(this.objMapper.writeValueAsString(value));
		} catch (Exception e) {
			throw new CryptoException(e);
		}
		return null;
	}

	@Override
	public T parse(String text, Locale locale) throws ParseException {
		T value = null;
		try {

			String word = text;
			word = decrypt(word);
			value = getValue(word, this.fieldType);

		} catch (IllegalArgumentException | SecurityException | JsonProcessingException e) {
			logger.error(ExceptionUtils.getStackTrace(e));
			throw new CryptoException(e);
		}
		return value;
	}

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
			} else if (String.class.isAssignableFrom(classField))
				value = (F) word;
			else
				value = this.objMapper.readValue(word, classField);
		}
		return value;
	}

	private String encrypt(T value) {
		String word = value.toString();
		return encryptValue(word);
	}

	protected abstract String encryptValue(String word);

	protected abstract String decrypt(String word);
}