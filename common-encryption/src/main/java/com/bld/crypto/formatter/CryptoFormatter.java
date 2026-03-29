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

/**
 * Abstract Spring {@link Formatter} that encrypts a value on {@link #print(Object, Locale)}
 * and decrypts it on {@link #parse(String, java.util.Locale)}.
 *
 * <p>Subclasses supply the actual encryption/decryption logic by implementing
 * {@link #encryptValue(String)} and {@link #decrypt(String)}.  The class handles
 * type conversion between the generic type {@code T} (which may be a {@link Number},
 * a {@link String}, or any JSON-serialisable POJO) and its string representation.
 *
 * @param <T> the field type handled by this formatter
 */
@SuppressWarnings("unchecked")
public abstract class CryptoFormatter<T> implements Formatter<T> {

	/** Jackson {@link ObjectMapper} used for JSON serialisation/deserialisation of POJO values. */
	private ObjectMapper objMapper;

	/** The Java type of the field that this formatter is attached to. */
	private Class<T> fieldType;

	/** Logger for this class. */
	private final static Logger logger = LoggerFactory.getLogger(CryptoFormatter.class);

	/**
	 * Constructs a new {@code CryptoFormatter}.
	 *
	 * @param objMapper the Jackson {@link ObjectMapper} used for JSON conversion
	 * @param fieldType the Java type of the annotated field
	 */
	public CryptoFormatter(ObjectMapper objMapper, Class<T> fieldType) {
		super();
		this.objMapper = objMapper;
		this.fieldType = fieldType;
	}

	/**
	 * Encrypts the given value and returns its cipher-text string representation.
	 * Numbers and strings are converted to their {@code toString()} form before
	 * encryption; POJOs are serialised to JSON first.
	 *
	 * @param value  the value to encrypt
	 * @param locale the current locale (unused, required by the {@link Formatter} contract)
	 * @return the encrypted string, or {@code null} if {@code value} is {@code null}
	 * @throws CryptoException if the encryption or JSON serialisation fails
	 */
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

	/**
	 * Decrypts the given cipher-text string and converts the result to the target
	 * field type.
	 *
	 * @param text   the encrypted string to parse
	 * @param locale the current locale (unused, required by the {@link Formatter} contract)
	 * @return the decrypted, type-converted value; {@code null} if the decrypted text is blank
	 * @throws ParseException   if the value cannot be parsed after decryption
	 * @throws CryptoException  if the decryption or JSON deserialisation fails
	 */
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

	/**
	 * Encrypts the given plain-text word using the concrete encryption strategy.
	 *
	 * @param word the plain-text string to encrypt
	 * @return the encrypted, Base64-encoded string
	 */
	protected abstract String encryptValue(String word);

	/**
	 * Decrypts the given cipher-text word using the concrete decryption strategy.
	 *
	 * @param word the Base64-encoded cipher text to decrypt
	 * @return the plain-text string
	 */
	protected abstract String decrypt(String word);
}