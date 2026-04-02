/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pkcs12.formatter.CryptoPkcs12AnnotationFormatterFactory.java
 */
package com.bld.crypto.pkcs12.formatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import com.bld.crypto.pkcs12.CryptoPkcs12Utils;
import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Spring {@link AnnotationFormatterFactory} that registers {@link CryptoPkcs12Formatter}
 * for fields annotated with {@link CryptoPkcs12}.
 *
 * <p>Supported field types: {@link String} and {@link Number}.
 */
public final class CryptoPkcs12AnnotationFormatterFactory implements AnnotationFormatterFactory<CryptoPkcs12> {

	/** Supported field types for the {@link CryptoPkcs12} annotation. */
	private static final Set<Class<?>> FIELD_TYPES = new HashSet<>(Arrays.asList(String.class, Number.class));

	/** The PKCS12 encryption utility. */
	private final CryptoPkcs12Utils cryptoPkcs12Utils;

	/** Jackson {@link ObjectMapper} used by the formatters for POJO conversion. */
	private final ObjectMapper objMapper;


	/**
	 * Constructs a new factory with the required dependencies.
	 *
	 * @param cryptoPkcs12Utils the encryption utility
	 * @param objMapper         the Jackson object mapper
	 */
	public CryptoPkcs12AnnotationFormatterFactory(CryptoPkcs12Utils cryptoPkcs12Utils, ObjectMapper objMapper) {
		super();
		this.cryptoPkcs12Utils = cryptoPkcs12Utils;
		this.objMapper = objMapper;
	}


	/**
	 * Returns the set of field types supported by this factory.
	 *
	 * @return {@link String} and {@link Number}
	 */
	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}


	/**
	 * Returns a {@link CryptoPkcs12Formatter} configured to encrypt values on print.
	 *
	 * @param annotation the resolved {@link CryptoPkcs12} annotation
	 * @param fieldType  the field type
	 * @return a printer that encrypts the field value
	 */
	@Override
	public Printer<?> getPrinter(CryptoPkcs12 annotation, Class<?> fieldType) {
		return new CryptoPkcs12Formatter<>(annotation, this.cryptoPkcs12Utils, this.objMapper, fieldType);
	}


	/**
	 * Returns a {@link CryptoPkcs12Formatter} configured to decrypt values on parse.
	 *
	 * @param annotation the resolved {@link CryptoPkcs12} annotation
	 * @param fieldType  the field type
	 * @return a parser that decrypts the field value
	 */
	@Override
	public Parser<?> getParser(CryptoPkcs12 annotation, Class<?> fieldType) {
		return new CryptoPkcs12Formatter<>(annotation, this.cryptoPkcs12Utils, this.objMapper, fieldType);
	}

}
